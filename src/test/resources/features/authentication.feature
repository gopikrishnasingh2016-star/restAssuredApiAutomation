@api @auth
Feature: Authentication
  As the platform
  I want credentials to be validated before a session is issued
  So that only legitimate clients receive a token

  @smoke
  Scenario: Log in with valid credentials
    When I log in with the email "eve.holt@reqres.in" and the password "cityslicka"
    Then the response status code should be 200
    And the response should match the "auth-success" schema
    And a session token should be returned
    And the response should arrive within 15000 milliseconds

  @regression @negative
  Scenario: Log in without a password
    When I log in with the email "eve.holt@reqres.in" and no password
    Then the response status code should be 400
    And the request should be rejected with the error "Missing password"

  @regression @negative
  Scenario: Log in without an email
    When I log in with the password "cityslicka" and no email
    Then the response status code should be 400
    And the request should be rejected with the error "Missing email or username"

  @smoke
  Scenario: Register a known user
    When I register with the email "eve.holt@reqres.in" and the password "pistol"
    Then the response status code should be 200
    And the response should match the "auth-success" schema
    And a session token should be returned

  @regression @negative
  Scenario: Register without a password
    When I register with the email "sydney@fife" and no password
    Then the response status code should be 400
    And the request should be rejected with the error "Missing password"
