Feature: Register User
  In order to use PickCar
  As a driver
  I want to register myself with my email and get an account

  Scenario: Register new user
    Given There is no registered user with email "user@sample.app"
    And I'm not logged in
    When I register a new user with email "user@sample.app" and password "password"
    Then The response code is 201
    And I can login with email "user@sample.app" and password "password"
    And It has been created a user with email "user@sample.app", the password is not returned
    And The user with email "user@sample.app" has role "DRIVER" and status "PENDING_EMAIL"

  Scenario: Cannot self-assign the admin role when registering
    Given I'm not logged in
    When I register a new user with email "user@sample.app", password "password" and role "ADMIN"
    Then The response code is 201
    And The user with email "user@sample.app" has role "DRIVER" and status "PENDING_EMAIL"

  Scenario: Register existing email
    Given There is a registered user with email "user@sample.app" and password "existing"
    And I'm not logged in
    When I register a new user with email "user@sample.app" and password "newpassword"
    Then The response code is 409
    And I cannot login with email "user@sample.app" and password "newpassword"

  Scenario: Register user when already authenticated
    Given I login as "demo@sample.app" with password "password"
    When I register a new user with email "user@sample.app" and password "password"
    Then The response code is 403
    And It has not been created a user with email "user@sample.app"

  Scenario: Register user with empty password
    Given I'm not logged in
    When I register a new user with email "user@sample.app" and password ""
    Then The response code is 400
    And The error message is "must not be blank"
    And It has not been created a user with email "user@sample.app"

  Scenario: Register user with empty email
    Given I'm not logged in
    When I register a new user with email "" and password "password"
    Then The response code is 400
    And The error message is "must not be blank"

  Scenario: Register user with invalid email
    Given I'm not logged in
    When I register a new user with email "userasample.app" and password "password"
    Then The response code is 400
    And The error message is "must be a well-formed email address"
    And It has not been created a user with email "userasample.app"

  Scenario: Register user with password shorter than 8 characters
    Given I'm not logged in
    When I register a new user with email "user@sample.app" and password "pass"
    Then The response code is 400
    And The error message is "length must be between 8 and 256"
    And It has not been created a user with email "user@sample.app"

  Scenario: Users cannot retrieve other users
    Given There is a registered user with email "user@sample.app" and password "password"
    And There is a registered user with email "another@sample.app" and password "password"
    And I login as "another@sample.app" with password "password"
    When I retrieve the user with email "user@sample.app"
    Then The response code is 404

  Scenario: Admin can retrieve other users
    Given There is a registered user with email "user@sample.app" and password "password"
    And I login as "admin@sample.app" with password "password"
    When I retrieve the user with email "user@sample.app"
    Then The response code is 200

  Scenario: Users cannot change their own role or status
    Given There is a registered user with email "user@sample.app" and password "password"
    And I login as "user@sample.app" with password "password"
    When I change the user with email "user@sample.app" to role "ADMIN" and status "ACTIVE"
    Then The user with email "user@sample.app" has role "DRIVER" and status "PENDING_EMAIL"
