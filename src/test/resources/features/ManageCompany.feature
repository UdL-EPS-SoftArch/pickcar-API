Feature: Manage Company
    In order to operate as a company
    As a user
    I want to register my company and not see or alter others'

    Background:
        Given There is a registered user with email "user@sample.app" and password "password"
        And There is a registered user with email "another@sample.app" and password "password"

    Scenario: The creator of a company owns it and it starts as PENDING
        Given I login as "user@sample.app" with password "password"
        When I create a new company with CIF "B12345678" and status "APPROVED"
        Then The response code is 201
        And The new company has status "PENDING"
        And The new company is owned by "user@sample.app"

    Scenario: A company without documents is rejected
        Given I login as "user@sample.app" with password "password"
        When I create a new company with CIF "B12345678" and no documents
        Then The response code is 400

    Scenario: Cannot retrieve a company owned by another user
        Given I login as "another@sample.app" with password "password"
        And There is a company with CIF "B12345678" owned by "user@sample.app"
        When I retrieve the company with CIF "B12345678"
        Then The response code is 404

    Scenario: A company owner cannot self-approve via PATCH
        Given I login as "user@sample.app" with password "password"
        And There is a company with CIF "B12345678" owned by "user@sample.app"
        When I try to change the status of the company with CIF "B12345678" to "APPROVED"
        Then The response code is 200
        And The company with CIF "B12345678" has status "PENDING"

    Scenario: A company owner cannot reassign the company to another user
        Given I login as "user@sample.app" with password "password"
        And There is a company with CIF "B12345678" owned by "user@sample.app"
        When I try to reassign the company with CIF "B12345678" to "another@sample.app"
        Then The response code is 200
        And The company with CIF "B12345678" is owned by "user@sample.app"
