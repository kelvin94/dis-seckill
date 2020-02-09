package com.jyl.authapi.authapi.test.service;

import com.jyl.authapi.authapi.Service.RoleService;
import com.jyl.authapi.authapi.Utility.AuthApiUtil;
import com.jyl.authapi.authapi.model.Role;
import com.jyl.authapi.authapi.repository.RoleRepository;
import com.jyl.authapi.authapi.resource.ApiResponse;
import com.jyl.authapi.authapi.resource.NewRoleRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
@RunWith(MockitoJUnitRunner.class)
public class RoleServiceTest {
    private  final static Logger logger = LogManager.getLogger(RoleServiceTest.class);
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private RoleService roleService;
    private NewRoleRequest requestParam = new NewRoleRequest();
    private Role role = new Role();
    private Role expectedRole = new Role();

    @Before
    public void setUp() {
        expectedRole.setRoleName(AuthApiUtil.ROLE_ADMIN);

    }

    @Test
    public void testCreateNewRole_happyCase() {
        requestParam.setRoleName(AuthApiUtil.ROLE_ADMIN);
        role.setRoleName(AuthApiUtil.ROLE_ADMIN);
        expectedRole.setRoleName(AuthApiUtil.ROLE_ADMIN);
        when(roleRepository.save(role)).thenReturn(expectedRole);
        ApiResponse actual = roleService.createNewRole(requestParam);
        assertNotNull(actual);
        assertTrue(actual.getSuccess());
        assertTrue(actual.getHttpStatus().toString().equalsIgnoreCase(HttpStatus.CREATED.toString()));
        verify(roleRepository, times(1)).save(role);

    }

    @Test(expected = DataAccessException.class)
    public void testCreateNewRole_UnableToSaveToDB() {
        requestParam.setRoleName(AuthApiUtil.ROLE_ADMIN);
        role.setRoleName(AuthApiUtil.ROLE_ADMIN);
        expectedRole.setRoleName(AuthApiUtil.ROLE_ADMIN);
        when(roleRepository.save(role)).thenThrow(new DataAccessException("..."){ });
        ApiResponse actual = roleService.createNewRole(requestParam);
        verify(roleRepository, times(1)).save(role);
    }

    @Test
    public void testCreateNewRole_DBQueryReturnNull() {
        requestParam.setRoleName(AuthApiUtil.ROLE_ADMIN);
        role.setRoleName(AuthApiUtil.ROLE_ADMIN);
        when(roleRepository.save(role)).thenReturn(null);
        ApiResponse actual = roleService.createNewRole(requestParam);
        assertNotNull(actual);
        assertFalse(actual.getSuccess());
        assertTrue(actual.getHttpStatus().toString().equalsIgnoreCase(HttpStatus.BAD_REQUEST.toString()));
        verify(roleRepository, times(1)).save(role);
    }

    // ====================== Unit test for deletRole
    @Test
    public void testDeleteRole_happyCase() {
        when(roleRepository.existsByRoleName(expectedRole.getRoleName())).thenReturn(true);
        when(roleRepository.findByRoleName(expectedRole.getRoleName())).thenReturn(expectedRole);
        ApiResponse actual = roleService.deleteRole(AuthApiUtil.ROLE_ADMIN);
        assertNotNull(actual);
        assertTrue(actual.getSuccess());
        assertTrue(actual.getHttpStatus().toString().equalsIgnoreCase(HttpStatus.OK.toString()));
        verify(roleRepository, times(1)).delete(expectedRole);
    }

    @Test
    public void testDeleteRole_RoleNameNotFOund() {
        when(roleRepository.existsByRoleName(expectedRole.getRoleName())).thenReturn(false);
        ApiResponse actual = roleService.deleteRole(AuthApiUtil.ROLE_ADMIN);
        assertNotNull(actual);
        assertFalse(actual.getSuccess());
        assertTrue(actual.getHttpStatus().toString().equalsIgnoreCase(HttpStatus.NOT_FOUND.toString()));
        verify(roleRepository, times(1)).existsByRoleName(expectedRole.getRoleName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteRole_PassIllegalArgsToDelete() {
        when(roleRepository.existsByRoleName(expectedRole.getRoleName())).thenReturn(true);
        when(roleRepository.findByRoleName(expectedRole.getRoleName())).thenReturn(null);
        doThrow(new IllegalArgumentException()).when(roleRepository).delete(null);
        roleService.deleteRole(AuthApiUtil.ROLE_ADMIN);
    }
}
