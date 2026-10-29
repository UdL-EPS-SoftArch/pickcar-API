Feature: Manage Document
    In order to prove my identity and my vehicles
    As a user
    I want to upload documents and see only my own

    Background:
        Given There is a registered user with username "user" and password "password" and email "user@sample.app"
        And There is a registered user with username "another" and password "password" and email "another@sample.app"
        And There is a document with file name "license.pdf" uploaded by "user"

    Scenario: Uploading a document marks it as uploaded by me
        Given I login as "user" with password "password"
        When I upload a document with file name "id.png" and size 2048
        Then The response code is 201
        And The new document has status "UPLOADED"
        And The new document is uploaded by "user"

    Scenario: Cannot choose the status of a document when uploading it
        Given I login as "user" with password "password"
        When I upload a document with file name "id.png" and size 2048 and status "APPROVED"
        Then The response code is 201
        And The new document has status "UPLOADED"

    Scenario: Cannot upload a document with a blank file name
        Given I login as "user" with password "password"
        When I upload a document with file name "" and size 2048
        Then The response code is 400
        And The error message is "must not be blank"

    Scenario: Cannot upload a document with a non positive size
        Given I login as "user" with password "password"
        When I upload a document with file name "id.png" and size 0
        Then The response code is 400
        And The error message is "must be greater than 0"

    Scenario: Cannot upload a document when not authenticated
        Given I'm not logged in
        When I upload a document with file name "id.png" and size 2048
        Then The response code is 401

    Scenario: Uploader can retrieve their own document
        Given I login as "user" with password "password"
        When I retrieve the document with file name "license.pdf"
        Then The response code is 200

    Scenario: Cannot retrieve a document uploaded by another user
        Given I login as "another" with password "password"
        When I retrieve the document with file name "license.pdf"
        Then The response code is 404

    Scenario: Uploader cannot approve their own document
        Given I login as "user" with password "password"
        When I change the status of the document with file name "license.pdf" to "APPROVED"
        Then The response code is 403
