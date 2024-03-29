package br.edu.utfpr.pb.pw25s.server;

import br.edu.utfpr.pb.pw25s.server.error.ApiError;
import br.edu.utfpr.pb.pw25s.server.model.User;
import br.edu.utfpr.pb.pw25s.server.repository.UserRepository;
import br.edu.utfpr.pb.pw25s.server.shared.GenericResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.INT_STREAM;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//@TestMethodOrder(MethodOrderer.MethodName.class)
public class UserControllerTest {

    private static final String API_USERS = "/users";
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    public void cleanup(){
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    //methodName_condition_expectedBehaviour
    @Test
    public void postUser_whenUserIsValid_receiveSuccessMessage(){
        User user = createValidUser();

        ResponseEntity<GenericResponse> response = postSignup( user, GenericResponse.class);

        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDatabase(){
        User user = createValidUser();
        postSignup(user, Object.class);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void postUser_whenUserIsValid_passwordIsHashedInDatabase(){
        User user = createValidUser();
        postSignup(user, Object.class);

        List<User> users = userRepository.findAll();
        User userDB = users.get(0);
        assertThat(userDB.getPassword()).isNotEqualTo(user.getPassword());
    }

    @Test
    public void postUser_whenUserHasNullUsername_receiveBadRequest(){
        User user = createValidUser();
        user.setUsername(null);

        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasUsernameLessThanRequired_receiveBadRequest(){
        User user = createValidUser();
        user.setUsername("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasDisplayNameLessThanRequired_receiveBadRequest(){
        User user = createValidUser();
        user.setDisplayName("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordLessThanRequired_receiveBadRequest(){
        User user = createValidUser();
        user.setPassword("abcef");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest(){
        User user = createValidUser();
        String stringWith256Chars = IntStream.rangeClosed(1, 256).mapToObj(x-> "a")
                        .collect(Collectors.joining());
        user.setUsername(stringWith256Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasDisplayNameExceedsTheLengthLimit_receiveBadRequest(){
        User user = createValidUser();
        String stringWith256Chars = IntStream.rangeClosed(1, 256).mapToObj(x-> "a")
                .collect(Collectors.joining());
        user.setDisplayName(stringWith256Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordAllLowerCase_receiveBadRequest(){
        User user = createValidUser();

        user.setPassword("password");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_WhenUserIsInvalid_receiveApiError(){
        User user = new User();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        assertThat(response.getBody().getUrl()).isEqualTo(API_USERS);

    }

    @Test
    public void postUser_WhenUserIsInvalid_receiveApiErrorWithValidationErrors(){
        User user = new User();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);

    }

    @Test
    public void postUser_WhenUserIsInvalid_receiveApiErrorWithValidationError(){
        User user = new User();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        assertThat(response.getBody().getUrl()).isEqualTo(API_USERS);

    }

    @Test
    public void postUser_WhenUserHasNullUsername_receiveMessageOfNullUsername(){
        User user = createValidUser();
        user.setUsername(null);
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();

        assertThat(validationErrors.get("username"))
                .isEqualTo("O 'usuário' não pode ser nulo");

    }

    @Test
    public void postUser_whenAnotherUsernameHasSameUsername_receiveBadRequest(){
        userRepository.save(createValidUser());

        User user = createValidUser();
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    public <T> ResponseEntity<T> postSignup(Object request, Class<T> responseType){
        return testRestTemplate.postForEntity(API_USERS, request, responseType);
    }

    private User createValidUser() {
        User user = new User();
        user.setUsername("test-user");
        user.setDisplayName("test-display");
        user.setPassword("P4ssword");

        return user;
    }
}
