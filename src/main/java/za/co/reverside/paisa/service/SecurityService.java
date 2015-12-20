package za.co.reverside.paisa.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.reverside.paisa.domain.User;
import static za.co.reverside.paisa.mapper.Mapper.toUserQueryModel;
import za.co.reverside.paisa.model.UserQueryModel;
import za.co.reverside.paisa.repository.UserRepository;


@RestController
public class SecurityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ResponseEntity<String> login(@RequestParam(value = "state", required = false, defaultValue = "/") String state) {
        try {
            state = URLEncoder.encode(state, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            state = "%2F";
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Location", "signin.html");
        headers.add("Set-Cookie", "state=" + state + ";");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public ResponseEntity<String> login(@RequestParam("userName") String email, @RequestParam("password") String password,
            @CookieValue(value = "state", required = false, defaultValue = "%2F") String state) {

        LOGGER.info("user login - userName:{}, password:{}, state:{}", email, password, state);

        User user = userRepository.findByEmailAndPassword(email, password);

        if (user != null) {
            try {
                state = URLDecoder.decode(state, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                state = "/";
            }
            LOGGER.info("Role : " + user.getRole());
            String token = user.getEmail() + ":" + user.getPassword();
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
            if (user.getRole().equals("USER")) {
                headers.add("Location", "loanTypes.html");
                headers.add("Set-Cookie", "token=" + new String(Base64.getEncoder().encode(token.getBytes())));
                headers.add("Set-Cookie", "state=; Max-Age=0");
                headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                headers.add("Pragma", "no-cache");
                headers.add("Expires", "0");

            } 
            return new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
        } else {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
            headers.add("Location", "signin.html");
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            return new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
        }
    }
    


    @RequestMapping(value = "validate", method = RequestMethod.POST, consumes = "text/html")
    public UserQueryModel validate(@RequestBody String token) {
        byte[] value = Base64.getDecoder().decode(token.getBytes());
        String username = new String(value).split(":")[0];
        String password = new String(value).split(":")[1];

        User user = userRepository.findByEmailAndPassword(username, password);

        if (user != null) {
            return toUserQueryModel(user);
        }
        throw new RuntimeException("Invalid Token");
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public ResponseEntity<String> logout() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Location", "signin.html");
        headers.add("Set-Cookie", "token=; Max-Age=0");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
    }
}
