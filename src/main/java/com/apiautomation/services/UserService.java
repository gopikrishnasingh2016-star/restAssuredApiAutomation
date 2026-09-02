package com.apiautomation.services;

import com.apiautomation.constants.Endpoints;
import com.apiautomation.models.request.CreateUserRequest;
import com.apiautomation.models.response.SingleUserResponse;
import com.apiautomation.models.response.UserListResponse;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Everything the {@code /users} resource can do. One class per resource keeps the
 * contract discoverable: if the API grows an endpoint, it grows a method here.
 */
public class UserService extends BaseService {

    @Step("Fetch page {page} of users")
    public Response getUsersPage(int page) {
        return get(Map.of("page", page), Endpoints.USERS);
    }

    @Step("Fetch page {page} of users (typed)")
    public UserListResponse getUsersPageAs(int page) {
        return getUsersPage(page).as(UserListResponse.class);
    }

    @Step("Fetch users with a server-side delay of {seconds}s")
    public Response getUsersWithDelay(int seconds) {
        return get(Map.of("delay", seconds), Endpoints.USERS);
    }

    @Step("Fetch user {id}")
    public Response getUser(int id) {
        return get(Endpoints.USER_BY_ID, id);
    }

    @Step("Fetch user {id} (typed)")
    public SingleUserResponse getUserAs(int id) {
        return getUser(id).as(SingleUserResponse.class);
    }

    @Step("Create user")
    public Response createUser(CreateUserRequest payload) {
        return post(payload, Endpoints.USERS);
    }

    @Step("Replace user {id}")
    public Response replaceUser(int id, CreateUserRequest payload) {
        return put(payload, Endpoints.USER_BY_ID, id);
    }

    @Step("Partially update user {id}")
    public Response updateUser(int id, CreateUserRequest payload) {
        return patch(payload, Endpoints.USER_BY_ID, id);
    }

    @Step("Delete user {id}")
    public Response deleteUser(int id) {
        return delete(Endpoints.USER_BY_ID, id);
    }
}
