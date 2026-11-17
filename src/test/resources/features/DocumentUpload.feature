Feature: Driver licence document upload
  As a driver
  I want to upload the front and back of my licence
  So that my profile can be verified

  Background:
    Given a driver profile exists for licence upload

  Scenario: Upload licence front
    When I upload the licence front "front.png" of type "image/png"
    Then a document "front.png" with status "UPLOADED" is linked to the profile
    And the profile licence front references that document

  Scenario: Upload licence back
    When I upload the licence back "back.pdf" of type "application/pdf"
    Then a document "back.pdf" with status "UPLOADED" is linked to the profile

  Scenario: Reject a licence file with an invalid type
    When I upload the licence front "front.exe" of type "application/x-msdownload"
    Then the licence upload fails with "Invalid file type"

  Scenario: Reject an empty licence file
    When I validate an empty licence front and a back "back.png" of type "image/png"
    Then the licence upload fails with "A licence file is required"

  Scenario: Re-upload after rejection
    Given the licence profile is rejected with reason "Blurry"
    When I re-upload the licence front "new-front.png" and back "new-back.png"
    Then the licence profile is "PENDING" with no rejection reason

  Scenario: Cannot re-upload when not rejected
    When I re-upload the licence front "new-front.png" and back "new-back.png"
    Then the licence upload fails with "only be re-uploaded after a rejection"
