Feature: Manage Record
    In order to manage user records
    As a user
    I want to be able to create, retrieve, edit and delete my records

    Background:
        Given There is a registered user with email "user@sample.app" and password "password"
        And There is a registered user with email "another@sample.app" and password "password"
        And There is a record with name "Existing Record" owned by "user@sample.app"

    Scenario: The creator of a record owns it
        Given I login as "user@sample.app" with password "password"
        When I create a new record with name "My Record"
        Then The response code is 201
        And The new record is owned by "user@sample.app"
        And The list of records owned by "user@sample.app" includes 1 named "My Record"

    Scenario: Cannot create a record with empty name
        Given I login as "user@sample.app" with password "password"
        When I create a new record with name ""
        Then The response code is 400
        And The error message is "must not be blank"

    Scenario: Creators can retrieve their own records
        Given I login as "user@sample.app" with password "password"
        When I retrieve the record with name "Existing Record"
        Then The response code is 200
        And The retrieved record has name "Existing Record"

    Scenario: Cannot retrieve a record owned by another user and by default private
        Given I login as "another@sample.app" with password "password"
        When I retrieve the record with name "Existing Record"
        Then The response code is 404

    Scenario: Can retrieve if the other user makes it public
        Given I login as "user@sample.app" with password "password"
        And I make the record with name "Existing Record" public
        And I login as "another@sample.app" with password "password"
        When I retrieve the record with name "Existing Record"
        Then The response code is 200

    Scenario: Creator can edit and modified timestamp is updated
        Given I login as "user@sample.app" with password "password"
        When I edit the record with name "Existing Record" to have name "Edited Record"
        Then The response code is 200
        And The list of records owned by "user@sample.app" includes 1 named "Edited Record"
        And The list of records owned by "user@sample.app" includes 0 named "Existing Record"
        And The modified timestamp of the record with name "Edited Record" is after the created one

    Scenario: Cannot edit a record owned by another user
        Given I login as "another@sample.app" with password "password"
        When I edit the record with name "Existing Record" to have name "Edited Record"
        Then The response code is 404
    
    Scenario: Creator can delete their own record
        Given I login as "user@sample.app" with password "password"
        When I delete the record with name "Existing Record"
        Then The response code is 204
        And The list of records owned by "user@sample.app" includes 0 named "Existing Record"

    Scenario: Cannot delete a record owned by another user
        Given I login as "another@sample.app" with password "password"
        When I delete the record with name "Existing Record"
        Then The response code is 404

    Scenario: Non-owner cannot edit a public record
        Given There is a public record with name "Public Record" owned by "user@sample.app"
        And I login as "another@sample.app" with password "password"
        When I edit the record with name "Public Record" to have name "Stolen Record"
        Then The response code is 403
        And The record with name "Public Record" is owned by "user@sample.app"

    Scenario: Non-owner cannot replace a public record
        Given There is a public record with name "Public Record" owned by "user@sample.app"
        And I login as "another@sample.app" with password "password"
        When I replace the record with name "Public Record" with name "Stolen Record"
        Then The response code is 403

    Scenario: Non-owner cannot delete a public record
        Given There is a public record with name "Public Record" owned by "user@sample.app"
        And I login as "another@sample.app" with password "password"
        When I delete the record with name "Public Record"
        Then The response code is 403
        And The record with name "Public Record" is owned by "user@sample.app"

    Scenario: A user cannot take ownership of a public record
        Given There is a public record with name "Public Record" owned by "user@sample.app"
        And I login as "another@sample.app" with password "password"
        When I set myself as owner of the record with name "Public Record"
        Then The response code is 403
        And The record with name "Public Record" is owned by "user@sample.app"

    Scenario: An admin can edit and delete another user's record
        Given There is a public record with name "Admin Managed Record" owned by "user@sample.app"
        And I login as "admin@sample.app" with password "password"
        When I edit the record with name "Admin Managed Record" to have name "Admin Edited Record"
        Then The response code is 200
        When I delete the record with name "Admin Edited Record"
        Then The response code is 204

    Scenario: A public record without an owner does not cause a server error
        Given There is a public record with name "Orphan Record" without an owner
        And I login as "another@sample.app" with password "password"
        When I edit the record with name "Orphan Record" to have name "Edited Orphan"
        Then The response code is 404