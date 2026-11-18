Feature: Driver licence document upload
  As a driver
  I want to upload the front and back of my licence
  So that my profile can be verified

  Background:
    Given a driver profile exists for licence upload

  Scenario: Upload driving licence
    When I upload the licence front "front.png" of type "image/png"
    And I upload the licence back "back.pdf" of type "application/pdf"
    Then a document "front.png" with status "UPLOADED" is linked to the profile
    And a document "back.pdf" with status "UPLOADED" is linked to the profile
    And the profile licence front references document "front.png"
    And the profile licence back references document "back.pdf"

  Scenario: Upload licence front
    When I upload the licence front "front.png" of type "image/png"
    Then a document "front.png" with status "UPLOADED" is linked to the profile
    And the profile licence front references document "front.png"

  Scenario: Upload licence back
    When I upload the licence back "back.pdf" of type "application/pdf"
    Then a document "back.pdf" with status "UPLOADED" is linked to the profile

  Scenario: Upload licence with only one side
    When I validate a licence front "front.png" of type "image/png" without a back side
    Then the licence upload fails with "A licence file is required"

  Scenario Outline: Upload a file with an invalid format or size
    When I upload the licence front "<file>" of type "<type>" and size <size> bytes
    Then the licence upload fails with "<message>"
    And no document "<file>" is stored

    Examples:
      | file      | type                     | size     | message                    |
      | front.exe | application/x-msdownload | 1024     | Invalid file type          |
      | front.gif | image/gif                | 1024     | Invalid file type          |
      | big.png   | image/png                | 10485761 | File size exceeds          |
      | empty.png | image/png                | 0        | A licence file is required |

  Scenario: Reject an empty licence file
    When I validate an empty licence front and a back "back.png" of type "image/png"
    Then the licence upload fails with "A licence file is required"

  Scenario: Re-upload documents after a rejection
    Given the licence profile is rejected with reason "Blurry"
    When I re-upload the licence front "new-front.png" and back "new-back.png"
    Then the licence profile is "PENDING" with no rejection reason
    And a document "new-front.png" with status "UPLOADED" is linked to the profile
    And the profile licence front references document "new-front.png"
    And the profile licence back references document "new-back.png"

  Scenario: Cannot re-upload when not rejected
    When I re-upload the licence front "new-front.png" and back "new-back.png"
    Then the licence upload fails with "only be re-uploaded after a rejection"
