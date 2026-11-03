Feature: DriverProfile Management
  As an API user
  I want to create and manage driver profiles
  So that driver registration can proceed

  Background:
    Given the application is running

  Scenario: Create a DriverProfile
    When I create a DriverProfile with user "user1" and verificationStatus "PENDING"
    Then the DriverProfile is created with status PENDING

  Scenario: Approve a DriverProfile
    Given a DriverProfile exists with status PENDING
    When I approve the DriverProfile
    Then the DriverProfile has status APPROVED

  Scenario: Reject a DriverProfile
    Given a DriverProfile exists with status PENDING
    When I reject the DriverProfile with reason "Invalid licence"
    Then the DriverProfile has status REJECTED
    And the DriverProfile has rejectionReason "Invalid licence"
