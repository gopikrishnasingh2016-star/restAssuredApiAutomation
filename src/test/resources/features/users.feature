@api @users
Feature: User directory
  As a consumer of the user service
  I want to read, create, update and delete users
  So that client applications can manage their user directory

  Background:
    Given the users API is reachable

  @smoke
  Scenario: Read a user that exists
    When I request the user with id 2
    Then the response status code should be 200
    And the response should be JSON
    And the response should match the "single-user" schema
    And the returned user should have the email "janet.weaver@reqres.in"

  @regression @negative
  Scenario: Read a user that does not exist
    When I request the user with id 23
    Then the response status code should be 404
    And the response body should be empty

  @regression
  Scenario Outline: Pages of the directory are returned with the requested page number
    When I request page <page> of users
    Then the response status code should be 200
    And the response should match the "user-list" schema
    And the response field "page" should be "<page>"
    And the page should contain <count> users

    Examples:
      | page | count |
      | 1    | 6     |
      | 2    | 6     |

  @smoke
  Scenario: Create a user
    When I create a user with generated details
    Then the response status code should be 201
    And the response should match the "created-user" schema
    And the created user should echo the name I sent

  @regression
  Scenario Outline: The directory accepts the documented range of names and job titles
    When I create a user named "<name>" with the job "<job>"
    Then the response status code should be 201
    And the response field "name" should be "<name>"
    And the response field "job" should be "<job>"

    Examples:
      | name               | job                          |
      | morpheus           | leader                       |
      | Ada Lovelace       | Analytical Engine Programmer |
      | Gopi Krishna Singh | SDET                         |

  @regression
  Scenario: Replace a user
    When I replace user 2 with the job "Team Lead"
    Then the response status code should be 200
    And the response should match the "updated-user" schema
    And the response field "job" should be "Team Lead"
    And the response field "updatedAt" should not be empty

  @smoke
  Scenario: Delete a user
    When I delete the user with id 2
    Then the response status code should be 204
    And the response body should be empty
