Feature: Manage Vehicle
    In order to rent out my vehicles
    As a company owner
    I want my vehicles created pending and approved only by an administrator

    Background:
        Given There is a registered user with email "owner@sample.app" and password "password"
        And There is a registered user with email "another@sample.app" and password "password"
        And There is a company with CIF "B12345678" owned by "owner@sample.app" with status "APPROVED"
        And There is a company with CIF "B87654321" owned by "another@sample.app" with status "APPROVED"
        And There is a company with CIF "B11111111" owned by "owner@sample.app" with status "PENDING"

    Scenario: A new vehicle starts as PENDING
        Given I login as "owner@sample.app" with password "password"
        When I create a vehicle with plate "1234ABC" for company "B12345678" with status "ACTIVE"
        Then The response code is 201
        And The vehicle with plate "1234ABC" has status "PENDING"

    Scenario: A vehicle cannot be created for a company owned by another user
        Given I login as "owner@sample.app" with password "password"
        When I create a vehicle with plate "1234ABC" for company "B87654321" with status "PENDING"
        Then The response code is 400

    Scenario: An owner cannot see a vehicle of another company
        Given I login as "another@sample.app" with password "password"
        And There is a vehicle with plate "1234ABC" for company "B12345678"
        When I retrieve the vehicle with plate "1234ABC"
        Then The response code is 404

    Scenario: A company owner cannot approve their own vehicle
        Given I login as "owner@sample.app" with password "password"
        And There is a vehicle with plate "1234ABC" for company "B12345678"
        When I change the status of the vehicle with plate "1234ABC" to "ACTIVE"
        Then The response code is 403
        And The vehicle with plate "1234ABC" has status "PENDING"

    Scenario: An admin can activate a vehicle of an approved company
        Given I login as "admin@sample.app" with password "password"
        And There is a vehicle with plate "1234ABC" for company "B12345678"
        When I change the status of the vehicle with plate "1234ABC" to "ACTIVE"
        Then The response code is 200
        And The vehicle with plate "1234ABC" has status "ACTIVE"

    Scenario: A vehicle of a pending company cannot be activated
        Given I login as "admin@sample.app" with password "password"
        And There is a vehicle with plate "1234ABC" for company "B11111111"
        When I change the status of the vehicle with plate "1234ABC" to "ACTIVE"
        Then The response code is 400
        And The vehicle with plate "1234ABC" has status "PENDING"

    Scenario: An invalid status transition is rejected
        Given I login as "admin@sample.app" with password "password"
        And There is a vehicle with plate "1234ABC" for company "B12345678" with status "REJECTED"
        When I change the status of the vehicle with plate "1234ABC" to "ACTIVE"
        Then The response code is 400
        And The vehicle with plate "1234ABC" has status "REJECTED"

    Scenario: The company of a vehicle cannot be changed
        Given I login as "admin@sample.app" with password "password"
        And There is a vehicle with plate "1234ABC" for company "B12345678"
        When I change the company of the vehicle with plate "1234ABC" to "B87654321"
        Then The response code is 403
        And The vehicle with plate "1234ABC" belongs to company "B12345678"
