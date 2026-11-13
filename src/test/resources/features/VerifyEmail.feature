Feature: Email Verification

  In order to complete the registration process
  As a newly registered user
  I want to verify my email using a verification token

  @email-verification
  Scenario: Verify a valid email token
    Given There is a registered user with email "user@sample.app"
    And The user has a pending verification token
    When I verify the email with a valid token
    Then The user status should be ACTIVE
    And The email should be verified at the current time
    And The verification token should be marked as VERIFIED

  @email-verification
  Scenario: Verify an expired email token
    Given There is a registered user with email "user@sample.app"
    And The user has a pending verification token that has expired
    When I verify the email with the expired token
    Then The verification token should be marked as EXPIRED
    And The user status should remain PENDING_EMAIL

  @email-verification
  Scenario: Verify an invalid email token
    Given There is a registered user with email "user@sample.app"
    When I verify the email with an invalid token
    Then The user status should remain PENDING_EMAIL