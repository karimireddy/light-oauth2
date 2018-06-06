package com.networknt.oauth.code.auth;

import com.networknt.oauth.code.ldap.LdapUtil;
import com.networknt.oauth.code.security.LightPasswordCredential;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

/**
 * This is the market place authentication class. It should be defined in the authenticate_class
 * column in client table when register the market place application so that this class will be
 * invoked when a user is login from market place.
 *
 * The current implementation for market place will use LDAP to do authentication and market place
 * roles configuration file for authorization. In the future, the authentication should be moved to
 * the SPNEGO/Kerberos and authorization will be from database.
 *
 * @author Steve Hu
 */
public class MarketPlaceAuthenticator extends AuthenticatorBase<MarketPlaceAuth> {
    private static final Logger logger = LoggerFactory.getLogger(MarketPlaceAuthenticator.class);
    private static final String USER_TYPE_EMPLOYEE = "employee";
    private static final String USER_TYPE_PUBLIC = "public";

    @Override
    public Account authenticate(String id, Credential credential) {

        LightPasswordCredential passwordCredential = (LightPasswordCredential) credential;
        char[] password = passwordCredential.getPassword();
        String userType = passwordCredential.getUserType();
        if(USER_TYPE_EMPLOYEE.equals(userType)) {
            // LDAP authentication and database authorization
            boolean authenticated = LdapUtil.authenticate(id, new String(password));
            if(authenticated) {
                // get role from db and construct an account object to return.
                Account account = new Account() {
                    private final Principal principal = () -> id;
                    @Override
                    public Principal getPrincipal() {
                        return principal;
                    }
                    @Override
                    public Set<String> getRoles() {
                        // TODO authorization for marketplace database?
                        return Collections.emptySet();
                    }
                };
                return account;
            } else {
                logger.error("Failed to authenticate user '" + id + "' with LDAP");
                return null;
            }
        } else {
            // Database authentication and authorization

        }
        return null;
    }
}
