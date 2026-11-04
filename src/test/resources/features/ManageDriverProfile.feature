Feature: Manage Driver Profile
    In order to manage driver registration
    As a user
    I want to create and retrieve my driver profile and not see others'

    Background:
        Given There is a registered user with email "user@sample.app" and password "password"
        And There is a registered user with email "another@sample.app" and password "password"

    Scenario: The creator of a driver profile owns it
        Given I login as "user@sample.app" with password "password"
        When I create a new driver profile
        Then The response code is 201
        And The new driver profile is owned by "user@sample.app"

    Scenario: A new driver profile always starts as PENDING
        Given I login as "user@sample.app" with password "password"
        When I create a new driver profile with verification status "APPROVED"
        Then The response code is 201
        And The new driver profile has verification status "PENDING"

    Scenario: Owner can retrieve their driver profile
        Given I login as "user@sample.app" with password "password"
        And There is a driver profile for user "user@sample.app"
        When I retrieve the driver profile for user "user@sample.app"
        Then The response code is 200

    Scenario: Cannot retrieve a driver profile owned by another user
        Given I login as "another@sample.app" with password "password"
        And There is a driver profile for user "user@sample.app"
        When I retrieve the driver profile for user "user@sample.app"
        Then The response code is 404

    Scenario: Cannot edit a driver profile owned by another user
        Given I login as "another@sample.app" with password "password"
        And There is a driver profile for user "user@sample.app"
        When I edit the licence front document of the driver profile for user "user@sample.app"
        Then The response code is 404
