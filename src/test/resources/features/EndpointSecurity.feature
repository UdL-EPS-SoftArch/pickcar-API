Feature: Endpoint security
    In order to protect user data
    As the system
    I want every endpoint to be denied by default unless explicitly allowed

    Background:
        Given There is a registered user with email "user@sample.app" and password "password"

    Scenario Outline: Anonymous users cannot reach deep or unknown paths
        Given I'm not logged in
        When I send a GET request to "<path>"
        Then The response code is 401

        Examples:
            | path                                 |
            | /records/search/findByName?name=x    |
            | /documents/search/findByFileName     |
            | /records/1/ownedBy                   |
            | /documents/1/uploadedBy              |
            | /unknown                             |
            | /unknown/path/deeper                 |

    Scenario Outline: Profile and explorer are not accessible
        Given I login as "user@sample.app" with password "password"
        When I send a GET request to "<path>"
        Then The response code is 403

        Examples:
            | path             |
            | /profile         |
            | /profile/records |
            | /explorer/index.html |

    Scenario Outline: Association endpoints cannot be modified
        Given I login as "user@sample.app" with password "password"
        When I send a <method> request to "/records/1/ownedBy"
        Then The response code is 403

        Examples:
            | method |
            | PUT    |
            | PATCH  |
            | DELETE |

    Scenario Outline: Unexported finders are not available
        Given I login as "user@sample.app" with password "password"
        When I send a GET request to "<path>"
        Then The response code is 404

        Examples:
            | path                                 |
            | /records/search/findByName?name=x    |
            | /documents/search/findByFileName?fileName=x |
