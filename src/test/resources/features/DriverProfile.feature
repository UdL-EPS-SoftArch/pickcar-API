Feature: DriverProfile Management
  As an API user
  I want to create and manage driver profiles
  So that driver registration can proceed

  Background:
    Given the application is running

  Scenario: Create a DriverProfile
    When I create a DriverProfile with user "user1" and verificationStatus "PENDING"
    Then the DriverProfile is created with status PENDING

  Scenario: Approve a DriverProfile as ADMIN
    Given I am authenticated as an ADMIN
    And a DriverProfile exists with status PENDING
    When I approve the DriverProfile
    Then the DriverProfile has status APPROVED

  Scenario: Reject a DriverProfile as ADMIN
    Given I am authenticated as an ADMIN
    And a DriverProfile exists with status PENDING
    When I reject the DriverProfile with reason "Invalid licence"
    Then the DriverProfile has status REJECTED
    And the DriverProfile has rejectionReason "Invalid licence"

  Scenario: A DRIVER cannot approve a DriverProfile through the service
    Given I am authenticated as a DRIVER
    And a DriverProfile exists with status PENDING
    When I try to approve the DriverProfile
    Then the approval is denied
    And the DriverProfile still has status PENDING

  Scenario: A DRIVER cannot reject a DriverProfile through the service
    Given I am authenticated as a DRIVER
    And a DriverProfile exists with status PENDING
    When I try to reject the DriverProfile
    Then the rejection is denied
    And the DriverProfile still has status PENDING
