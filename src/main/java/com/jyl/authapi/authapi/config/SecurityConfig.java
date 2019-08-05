package com.jyl.authapi.authapi.config;


import com.jyl.authapi.authapi.Security.JwtAuthenticationEntryPoint;
import com.jyl.authapi.authapi.Security.JwtAuthenticationFilter;
import com.jyl.authapi.authapi.Service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity //spring security annotation that is used to enable web security in a project.
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
) /*
EnableGlobalMethodSecurity - This is used to enable method level security based on annotations. You can use following
 three types of annotations for securing your methods
 “securedEnabled": It enables the @Secured annotation using which you can protect your controller/service methods
 like so -

        @Secured("ROLE_ADMIN") // The @Secured annotation is used to specify a list of roles on a method. Hence, a
        user only can access that method if she has at least one of the specified roles.
        public User getAllUsers() {}

        @Secured({"ROLE_USER", "ROLE_ADMIN"})
        public User getUser(Long id) {}

        @Secured("IS_AUTHENTICATED_ANONYMOUSLY")
        public boolean isUsernameAvailable() {}

 ”jsr250Enabled“: It enables the @RolesAllowed annotation that can be used like this -

                @RolesAllowed("ROLE_ADMIN")
                public Poll createPoll() {}
 -----------------@Secured - @RolesAllowed 区别：功能上来说是一样，但是 @RolesAllowed 来自Standard annotation of Java;
 @Secured来自Spring Security annotation

 "prePostEnabled": It enables more complex expression based access control syntax with @PreAuthorize and
 @PostAuthorize annotations:

        @PreAuthorize("isAnonymous()")
        public boolean isUsernameAvailable() {}

        @PreAuthorize("hasRole('USER')")
        public Poll createPoll() {}
*/
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /*
   WebSecurityConfigurerAdapter implements Spring Security’s WebSecurityConfigurer interface. It provides default
   security configurations and allows other classes to extend it and customize the security configurations by
   overriding its methods.

     */



    /*
    To authenticate a User or perform various role-based checks, Spring security needs to load users details somehow.

    For this purpose, It consists of an interface called UserDetailsService which has a single method that loads a
    user based on username-
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    We’ll define a CustomUserDetailsService that implements UserDetailsService interface and provides the
    implementation for loadUserByUsername() method.

    Note that, the loadUserByUsername() method returns a UserDetails object that Spring Security uses for performing
    various authentication and role based validations.

    We’ll also define a custom UserPrincipal class that will implement UserDetails interface, and return the
    UserPrincipal object from loadUserByUsername() method.
     */

    @Autowired
    CustomUserDetailsService customUserDetailsService;


    /*
    JwtAuthenticationEntryPoint class is used to return a 401 unauthorized error to clients that try to access a
    protected resource without proper authentication. It implements Spring Security’s AuthenticationEntryPoint
    interface.
    AuthenticationEntryPoint can be used to set necessary response headers, content-type, and so on before sending
    the response back to the client.


     */
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    /*
    We’ll use JWTAuthenticationFilter to implement a filter that：

                    -reads JWT authentication token from the Authorization header of all the requests
                    -validates the token
                    -loads the user details associated with that token.
                    -Sets the user details in Spring Security’s SecurityContext. Spring Security uses the user
                    details to perform authorization checks. We can also access the user details stored in the
                    SecurityContext in our controllers to perform our business logic.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /*
    AuthenticationManagerBuilder is used to create an AuthenticationManager instance which is the main Spring
    Security interface for authenticating a user.

    You can use AuthenticationManagerBuilder to build in-memory authentication, LDAP authentication, JDBC
    authentication, or add your custom authentication provider.

    In our example, we’ve provided our customUserDetailsService and a passwordEncoder to build the
    AuthenticationManager.

    We’ll use the configured AuthenticationManager to authenticate a user in the login API.
     */
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /*
        The HttpSecurity configurations are used to configure security functionalities like csrf, sessionManagement,
        and add rules to protect resources based on various conditions.

        In our example, we’re permitting access to static resources and few other public APIs to everyone and
        restricting access to other APIs to authenticated users only.
         */


        http
                .cors()
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js")
                .permitAll()
                .antMatchers("/api/auth/**")
                .permitAll()
                .antMatchers("/api/user/checkUsernameAvailability", "/api/user/checkEmailAvailability")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/api/polls/**", "/api/users/**")
                .permitAll()
                .anyRequest()
                .authenticated();


        /*
        CORS note:
        A request for a resource (like an image or a font) outside of the origin is known as a cross-origin request.
        CORS (cross-origin resource sharing) manages cross-origin requests.

        Once again, consider the following URL:


        http://www.example.com/foo-bar.html
        Let’s call it URL1 (for short).

        Unlike same-origin, navigating to https://www.ejemplo.com/hola.html from URL1 could be allowed with CORS.
        Allowing cross-origin requests is helpful, as many websites today load resources from different places on the
         Internet (stylesheets, scripts, images, and more).

        Cross-origin requests, however, mean that servers must implement ways to handle requests from origins outside
         of their own. CORS allows servers to specify who (i.e., which origins) can access the assets on the server,
         among many other things.

        You can think of these interactions as a building with a security entrance. For example, if you need to
        borrow a ladder, you could ask a neighbor in the building who has one. The building’s security would likely
        not have a problem with this request (i.e., same-origin). If you needed a particular tool, however, and you
        ordered it from an outside source like an online marketplace (i.e., cross-origin), the security at the
        entrance may request that the delivery person provide identification when your tool arrives.

    WHY IS CORS NECESSARY?
    The CORS standard is needed because it allows servers to specify not just who can access its assets, but also how
     the assets can be accessed.

    Cross-origin requests are made using the standard HTTP request methods. Most servers will allow GET requests,
    meaning they will allow resources from external origins (say, a web page) to read their assets. HTTP requests
    methods like PATCH, PUT, or DELETE, however, may be denied to prevent malicious behavior. For many servers, this
    is intentional. For example, it is likely that server A does not want servers B, C, or D to edit or delete its
    assets.

    With CORS, a server can specify who can access its assets and which HTTP request methods are allowed from
    external resources.

         */


        // Add our custom JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    }
}
