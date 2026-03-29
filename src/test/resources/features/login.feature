Feature: Login

  Scenario: Login fails with wrong password
    Given a user "student1" exists with password "Student@123" and role "STUDENT"
    When a login request is sent with username "student1" and password "wrong"
    Then the response status should be 401
    And the response message should be "Invalid username or password."

  Scenario: Login succeeds with correct password
    Given a user "student1" exists with password "Student@123" and role "STUDENT"
    When a login request is sent with username "student1" and password "Student@123"
    Then the response status should be 200
    And the response json should contain "role" = "STUDENT"