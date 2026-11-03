Feature: Manage Driver Profile
    In order to manage driver registration
    As a user
    I want to be able to create, retrieve, edit and delete my driver profile

    Background:
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And There is a registered user with username "another" and password "password" and email "another@sample.app"

    Scenario: The creator of a driver profile owns it
        Given I login as "user" with password "password"
        When I create a new driver profile for user "user"
        Then The response code is 201
        And The new driver profile is owned by "user"

    Scenario: Cannot retrieve a driver profile owned by another user
        Given I login as "user" with password "password"
        And There is a driver profile for user "user"
        When I retrieve the driver profile for user "user"
        Then The response code is 200

    Scenario: Creator can edit their driver profile
        Given I login as "user" with password "password"
        And There is a driver profile for user "user"
        When I edit the driver profile for user "user" to have verification status APPROVED
        Then The response code is 200
        And The driver profile for user "user" has verification status APPROVED

    Scenario: Cannot edit a driver profile owned by another user
        Given I login as "another" with password "password"
        And There is a driver profile for user "user"
        When I edit the driver profile for user "user" to have verification status APPROVED
        Then The response code is 404
