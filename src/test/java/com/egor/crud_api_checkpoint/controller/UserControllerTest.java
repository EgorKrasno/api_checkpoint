package com.egor.crud_api_checkpoint.controller;

import com.egor.crud_api_checkpoint.model.User;
import com.egor.crud_api_checkpoint.repository.UserRepository;
import com.egor.crud_api_checkpoint.request.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.transaction.Transactional;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    User user1;
    User user2;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "user1@email.com", "123456");
        user2 = new User(2L, "user2@email.com", "password");
        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    @Transactional
    @Rollback
    void getUsers() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/users");
        mvc.perform(request).andExpect(jsonPath("$.length()", is(2)));
        mvc.perform(request).andExpect(jsonPath("$[0].email", is(user1.getEmail())));
        mvc.perform(request).andExpect(jsonPath("$[1].email", is(user2.getEmail())));
    }

    @Test
    @Transactional
    @Rollback
    void createUser() throws Exception {
        User userToAdd = new User(3L, "user3@email.com", "123");
        String json = mapper.writeValueAsString(userToAdd);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        ResultActions result = mvc.perform(request);

        Optional<User> foundUser = userRepository.findByEmail(userToAdd.getEmail());
        assertTrue(foundUser.isPresent());
        result.andExpect(jsonPath("$.email", is(userToAdd.getEmail())));
        result.andExpect(jsonPath("$.id").isNotEmpty());
        result.andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @Transactional
    @Rollback
    public void getUser() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/users/2");
        ResultActions result = mvc.perform(request);

        result.andExpect(jsonPath("$.email", is(user2.getEmail())));
        result.andExpect(jsonPath("$.id").exists());
    }


    @Test
    @Transactional
    @Rollback
    public void patchUserEmail() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail("patched@email.com");
        String json = mapper.writeValueAsString(userRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        ResultActions result = mvc.perform(request);

        assertTrue(userRepository.findByEmail(userRequest.getEmail()).isPresent());
        assertFalse(userRepository.findByEmail(user2.getEmail()).isPresent());
        assertEquals(userRepository.count(), 2);
        result.andExpect(jsonPath("$.email", is(userRequest.getEmail())));
        result.andExpect(jsonPath("$.id", is(2)));
    }

    @Test
    @Transactional
    @Rollback
    public void patchUserPassword() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setPassword("newPassword");
        userRequest.setEmail("patched@email.com");
        String json = mapper.writeValueAsString(userRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        ResultActions result = mvc.perform(request);

        assertEquals(userRepository.findByEmail(userRequest.getEmail()).get().getPassword(), "newPassword");
        result.andExpect(jsonPath("$.email", is(userRequest.getEmail())));
        result.andExpect(jsonPath("$.id", is(2)));
    }

    @Test
    @Transactional
    @Rollback
    public void deleteUser() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/users/2");
        ResultActions result = mvc.perform(request);

        assertFalse(userRepository.findByEmail(user2.getEmail()).isPresent());
        result.andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    @Transactional
    @Rollback
    public void authenticateUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail("user1@email.com");
        userRequest.setPassword("123456");
        String json = mapper.writeValueAsString(userRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        ResultActions result = mvc.perform(request);

        result.andExpect(jsonPath("$.authenticated", is(true)));
        result.andExpect(jsonPath("$.user.email", is(user1.getEmail())));
        result.andExpect(jsonPath("$.user.id", is(1)));
    }

    @Test
    @Transactional
    @Rollback
    public void failAuthenticateUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail("user2@email.com");
        userRequest.setPassword("123456");
        String json = mapper.writeValueAsString(userRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        ResultActions result = mvc.perform(request);

        result.andExpect(jsonPath("$.authenticated", is(false)));
        result.andExpect(jsonPath("$.user.email").doesNotExist());
        result.andExpect(jsonPath("$.user.id").doesNotExist());
    }


}