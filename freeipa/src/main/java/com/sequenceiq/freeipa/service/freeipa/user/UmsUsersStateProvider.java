package com.sequenceiq.freeipa.service.freeipa.user;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.GetRightsResponse;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.Group;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.MachineUser;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.ResourceRoleAssignment;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.RoleAssignment;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.User;
import com.sequenceiq.cloudbreak.auth.altus.GrpcUmsClient;
import com.sequenceiq.cloudbreak.auth.altus.exception.UmsOperationException;
import com.sequenceiq.freeipa.service.freeipa.user.model.FmsGroup;
import com.sequenceiq.freeipa.service.freeipa.user.model.FmsUser;
import com.sequenceiq.freeipa.service.freeipa.user.model.UsersState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UmsUsersStateProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsUsersStateProvider.class);

    // TODO refactor ThreadBasedRequestIdProvider and replace this ThreadLocal
    private static ThreadLocal<String> requestId = new ThreadLocal<>();

    @Inject
    private GrpcUmsClient umsClient;

    private final String environmentWrite = "environments/write";

    public Map<String, UsersState> getEnvToUmsUsersStateMap(String accountId, String actorCrn, Set<String> environmentsFilter,
                                                            Set<String> userCrns, Set<String> machineUserCrns, Optional<String> requestId) {
        try {
            Optional<String> requestIdOptional =
                (requestId.isPresent()) ? requestId : Optional.of(UUID.randomUUID().toString());
            UmsUsersStateProvider.requestId.set(requestIdOptional.get());
            LOGGER.debug("Getting UMS state for environments {} with requestId {}", environmentsFilter, requestIdOptional);

            Map<String, UsersState> envUsersStateMap = new HashMap<>();


            List<User> users = userCrns.isEmpty() ? umsClient.listAllUsers(actorCrn, accountId, requestIdOptional)
                    : umsClient.listUsers(actorCrn, accountId, List.copyOf(userCrns), requestIdOptional);
            List<MachineUser> machineUsers = machineUserCrns.isEmpty() ? umsClient.listAllMachineUsers(actorCrn, accountId, requestIdOptional)
                    : umsClient.listMachineUsers(actorCrn, accountId, List.copyOf(machineUserCrns), requestIdOptional);

            Map<String, FmsGroup> crnToFmsGroup = umsClient.listGroups(actorCrn, accountId, List.of(), requestIdOptional).stream()
                    .collect(Collectors.toMap(Group::getCrn, this::umsGroupToGroup));

            environmentsFilter.stream().forEach(envCRN -> {
                UsersState.Builder userStateBuilder = new UsersState.Builder();

                users.stream().forEach(u -> {
                    FmsUser fmsUser = umsUserToUser(u);
                    handleUser(userStateBuilder, crnToFmsGroup, actorCrn, accountId, u.getCrn(), fmsUser, envCRN);
                });

                machineUsers.stream().forEach(mu -> {
                    FmsUser fmsUser = umsMachineUserToUser(mu);
                    handleUser(userStateBuilder, crnToFmsGroup, actorCrn, accountId, mu.getCrn(), fmsUser, envCRN);
                });

                envUsersStateMap.put(envCRN, userStateBuilder.build());
            });

            return envUsersStateMap;
        } catch (RuntimeException e) {
            throw new UmsOperationException(String.format("Error during UMS operation: {}", e));
        } finally {
            UmsUsersStateProvider.requestId.remove();
        }
    }

    private boolean isEnvironmentUser(String enviromentCrn, GetRightsResponse rightsResponse) {

        List<RoleAssignment> rolesAssignedList = rightsResponse.getRoleAssignmentList();
        for (RoleAssignment roleAssigned : rolesAssignedList) {
            // TODO: should come from IAM Roles and check against Role Object
            if (roleAssigned.getRole().getCrn().contains("PowerUser") ||
                roleAssigned.getRole().getCrn().contains("EnvironmentAdmin")) {
                return true;
                // admins are also users
            }
        }

        List<ResourceRoleAssignment> resourceRoleAssignedList = rightsResponse.getResourceRolesAssignmentList();
        for (ResourceRoleAssignment resourceRoleAssigned : resourceRoleAssignedList) {
            // TODO: should come from IAM Roles and check against Role Object
            if (resourceRoleAssigned.getResourceRole().getCrn().contains("EnvironmentAdmin") ||
                (resourceRoleAssigned.getResourceRole().getCrn().contains("EnvironmentUser"))) {
                return true;
            }
        }

        return false;
    }

    private boolean isEnvironmentAdmin(String enviromentCrn, GetRightsResponse rightsResponse) {
        List<RoleAssignment> rolesAssignedList = rightsResponse.getRoleAssignmentList();
        for (RoleAssignment roleAssigned : rolesAssignedList) {
            // TODO: should come from IAM Roles and check against Role Object
            if (roleAssigned.getRole().getCrn().contains("PowerUser") ||
                roleAssigned.getRole().getCrn().contains("EnvironmentAdmin")) {
                return true;
            }
        }

        List<ResourceRoleAssignment> resourceRoleAssignedList = rightsResponse.getResourceRolesAssignmentList();
        for (ResourceRoleAssignment resourceRoleAssigned : resourceRoleAssignedList) {
            // TODO: should come from IAM Roles and check against Role Object
            if (resourceRoleAssigned.getResourceRole().getCrn().contains("EnvironmentAdmin")) {
                return true;
            }
        }

        return false;
    }

    private void handleUser(UsersState.Builder userStateBuilder, Map<String, FmsGroup> crnToFmsGroup,
            String actorCrn, String accountId, String memberCrn, FmsUser fmsUser, String environmentCrn) {
        Optional<String> requestIdOptional = Optional.ofNullable(requestId.get());

        GetRightsResponse rightsResponse = umsClient.getRightsForUser(actorCrn, memberCrn, environmentCrn, requestIdOptional);
        if (isEnvironmentUser(environmentCrn, rightsResponse)) {
            userStateBuilder.addUser(fmsUser);
            // TODO get group membership from GetRightsResponse instead of separate call
            getGroupCrnsForMember(actorCrn, accountId, memberCrn).stream().forEach(gcrn -> {
                userStateBuilder.addMemberToGroup(crnToFmsGroup.get(gcrn).getName(), fmsUser.getName());
            });
            if (isEnvironmentAdmin(environmentCrn, rightsResponse)) {
                userStateBuilder.addMemberToGroup("admins", fmsUser.getName());
            }
        }
    }

    private List<String> getGroupCrnsForMember(
            String accountId, String actorCrn, String memberCrn) {
        Optional<String> requestIdOptional = Optional.ofNullable(requestId.get());

        return umsClient.listGroupsForMember(actorCrn, accountId, memberCrn, requestIdOptional);
    }

    private FmsUser umsUserToUser(User umsUser) {
        FmsUser fmsUser = new FmsUser();
        fmsUser.setName(umsUser.getWorkloadUsername());
        fmsUser.setFirstName(getOrDefault(umsUser.getFirstName(), "None"));
        fmsUser.setLastName(getOrDefault(umsUser.getLastName(), "None"));
        return fmsUser;
    }

    private String getOrDefault(String value, String other) {
        return (value == null || value.isBlank()) ? other : value;
    }

    private FmsUser umsMachineUserToUser(MachineUser umsMachineUser) {
        FmsUser fmsUser = new FmsUser();
        fmsUser.setName(umsMachineUser.getWorkloadUsername());
        // TODO what should the appropriate first and last name be for machine users?
        fmsUser.setFirstName("Machine");
        fmsUser.setLastName("User");
        return fmsUser;
    }

    private FmsGroup umsGroupToGroup(Group umsGroup) {
        FmsGroup fmsGroup = new FmsGroup();
        fmsGroup.setName(umsGroup.getGroupName());
        return fmsGroup;
    }

}
