@Sanity @API
Feature: Check basic POSTs RestApi endpoints

  @env_main @precon
  Scenario: Create a new person
    Given API: I create a new person
    And API: I check that POST call status code is 200

  @env_main @precon
  Scenario: Create new case
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new case
    And API: I check that POST call status code is 200

  @env_main @precon
  Scenario: Create a new contact
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new contact
    And API: I check that POST call status code is 200

  @env_main @precon
  Scenario: Create a new contact linked to a case
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new case
    And API: I check that POST call status code is 200
    Then API: I create a new contact linked to the previous created case
    And API: I check that POST call status code is 200

  @env_main @precon
  Scenario: Create a new event
    Given API: I create a new event
    And API: I check that POST call status code is 200

  @env_main @precon
  Scenario: Create a new sample
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new case
    And API: I check that POST call status code is 200
    Then API: I create a new sample
    And API: I check that POST call status code is 200

  @env_main @precon
  Scenario: Create a new task
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new contact
    And API: I check that POST call status code is 200
    Given API: I create a new task
    And API: I check that POST call status code is 200

  @env_main @precon
  Scenario Outline: Create Person and attach <numberOfImmunizations> immunizations
    Given API: I create a new person
    And API: I check that POST call status code is 200
    When API: I create <numberOfImmunizations> new immunizations for last created person
    And API: I check that POST call status code is 200

    Examples:
      | numberOfImmunizations |
      | 1                     |
      | 5                     |

  @env_de @precon
  Scenario: Create a new person on DE market
    Given API: I create a new person
    And API: I check that POST call status code is 200

  @env_de @precon
  Scenario: Create new case on DE market
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new case
    And API: I check that POST call status code is 200

  @env_de @precon
  Scenario: Create a new contact on DE market
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new contact
    And API: I check that POST call status code is 200

  @env_de @precon
  Scenario: Create a new contact linked to a case on DE market
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new case
    And API: I check that POST call status code is 200
    Then API: I create a new contact linked to the previous created case
    And API: I check that POST call status code is 200

  @env_de @precon
  Scenario: Create a new event on DE market
    Given API: I create a new event
    And API: I check that POST call status code is 200

  @env_de @precon
  Scenario: Create a new sample on DE market
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new case
    And API: I check that POST call status code is 200
    Then API: I create a new sample
    And API: I check that POST call status code is 200

  @env_de @precon
  Scenario: Create a new task on DE market
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new contact
    And API: I check that POST call status code is 200
    Given API: I create a new task
    And API: I check that POST call status code is 200

  @env_de @precon
  Scenario Outline: Create Person and attach <numberOfImmunizations> immunizations on DE market
    Given API: I create a new person
    And API: I check that POST call status code is 200
    When API: I create <numberOfImmunizations> new immunizations for last created person
    And API: I check that POST call status code is 200

    Examples:
      | numberOfImmunizations |
      | 1                     |
      | 5                     |

  @env_de @oldfake
  Scenario: Create new case with creation date 10 years ago
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new case with creation date 3653 days ago
    And API: I check that POST call status code is 200

  @env_de @oldfake
  Scenario: Create a new contact with creation date 5 years ago
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new contact with creation date 1827 days ago
    And API: I check that POST call status code is 200

  @env_de @oldfake
  Scenario: Create a new event with creation date 5 years ago
    Given API: I create a new event with creation date 1827 days ago
    And API: I check that POST call status code is 200

  @env_main @oldfake
  Scenario: Create Person and attach immunizations with creation date 10 years ago
    Given API: I create a new person
    And API: I check that POST call status code is 200
    When API: I create a new immunizations for last created person with creation date 3653 days ago
    And API: I check that POST call status code is 200

  @env_de @oldfake
  Scenario: Create Event participant with creation date 5 years ago
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new event
    And API: I check that POST call status code is 200
    Then API: I create a new event participant with creation date 1827 days ago
    And API: I check that POST call status code is 200

  @env_de @oldfake
  Scenario: Create Travel entry with creation date 14 days ago
    Given API: I create a new person
    And API: I check that POST call status code is 200
    Then API: I create a new travel entry with creation date 14 days ago
    And API: I check that POST call status code is 200

