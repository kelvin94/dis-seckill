package com.jyl.authapi.authapi.test.service;

import com.jyl.authapi.authapi.Service.InvitationCodeService;
import com.jyl.authapi.authapi.Utility.AuthApiUtil;
import com.jyl.authapi.authapi.model.InvitationCode;
import com.jyl.authapi.authapi.model.Role;
import com.jyl.authapi.authapi.repository.InvitationCodeRepository;
import com.jyl.authapi.authapi.repository.RoleRepository;
import com.jyl.authapi.authapi.resource.NewCodeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.dao.DataAccessException;

//@RunWith(MockitoJUnitRunner.class)

@RunWith(PowerMockRunner.class)
@PrepareForTest(AuthApiUtil.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public class CodeServiceTest {
    private  final static Logger logger = LogManager.getLogger(CodeServiceTest.class);

    private NewCodeRequest requestParam = new NewCodeRequest();
    private String invitationCode = "someinvitationCode";
    private InvitationCode code = new InvitationCode();
    private Role role = new Role();
    @Mock
    private AuthApiUtil authApiUtil;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private InvitationCodeRepository invitationCodeRepository;

    @InjectMocks
    private InvitationCodeService invitationCodeService;

    @Before
    public void setUp() {
        requestParam.setRoleType(AuthApiUtil.ROLE_ADMIN);
        role.setRoleName(AuthApiUtil.ROLE_ADMIN);

        code.setCode(invitationCode);
    }
    @Test
    public void testCreateNewCode_happyCase() throws Exception {
        PowerMockito.mockStatic(AuthApiUtil.class);

        when(roleRepository.existsByRoleName(requestParam.getRoleType())).thenReturn(true);
        when(AuthApiUtil.generateRandomString(AuthApiUtil.randomInvitationCodeLength)).thenReturn(invitationCode);
        when(roleRepository.findByRoleName(AuthApiUtil.ROLE_ADMIN)).thenReturn(role);

        code.setRole(role);
        when(invitationCodeRepository.save(code)).thenReturn(code);
        String actualCode = invitationCodeService.createCode(requestParam);
        assertNotNull(actualCode);
        assertTrue(actualCode.equalsIgnoreCase(invitationCode));
    }

    @Test
    public void testCreateNewCode_RoleNotExist() throws Exception {
        when(roleRepository.existsByRoleName("randomROle")).thenReturn(false);
        requestParam.setRoleType("randomROle");
        String actual = invitationCodeService.createCode(requestParam);
        logger.info("actual "+actual);
        assertNotNull(actual);
        assertTrue(actual.indexOf("role type does not exist") != -1);
    }

    @Test(expected = DataAccessException.class)
    public void testCreateNewCode_UnsuccessfulSaveToDB() throws Exception {
        PowerMockito.mockStatic(AuthApiUtil.class);

        when(roleRepository.existsByRoleName(requestParam.getRoleType())).thenReturn(true);
        when(AuthApiUtil.generateRandomString(AuthApiUtil.randomInvitationCodeLength)).thenReturn(invitationCode);
        when(roleRepository.findByRoleName(AuthApiUtil.ROLE_ADMIN)).thenReturn(role);

        code.setRole(role);
        when(invitationCodeRepository.save(code)).thenThrow(new DataAccessException("..."){});;
        String actualCode = invitationCodeService.createCode(requestParam);
    }
}
