# Author: your.email@your.domain.com
# Keywords Summary :
# Feature: List of scenarios.
# Scenario: Business rule through list of steps with arguments.
# Given: Some precondition step
# When: Some key actions
# Then: To observe outcomes or validation
# And,But: To enumerate more Given,When,Then steps
# Scenario Outline: List of steps for data-driven as an Examples and <placeholder>
# Examples: Container for s table
# Background: List of steps run before each of the scenarios
# """ (Doc Strings)
# | (Data Tables)
# @ (Tags/Labels):To group Scenarios
# <> (placeholder)
# ""
# # (Comments)
# Sample Feature Definition Template
Feature: Login, Add to Cart, and Checkout on SauceDemo

  # Positive case dengan data bervariasi menggunakan Scenario Outline
  Scenario Outline: Successful login and checkout with valid users
    Given user is on the SauceDemo login page
    When user logs in with username "<username>" and password "<password>"
    Then user should see the products page
    When user adds "Sauce Labs Backpack" to the cart
    And user proceeds to checkout
    And user fills in checkout information with first name "John", last name "Doe", and postal code "12345"
    And user completes the checkout
    Then user should see the order confirmation message "Thank you for your order!"

    Examples:
      | username                | password     |
      | standard_user           | secret_sauce |
      | performance_glitch_user | secret_sauce |

  # Negative case: login gagal karena akun locked out
  Scenario: Login fails with locked out user
    Given user is on the SauceDemo login page
    When user logs in with username "locked_out_user" and password "secret_sauce"
    Then user should see an error message "Epic sadface: Sorry, this user has been locked out."

  # Negative case: login gagal karena password salah
  Scenario: Login fails with incorrect password
    Given user is on the SauceDemo login page
    When user logs in with username "standard_user" and password "wrong_password"
    Then user should see an error message "Epic sadface: Username and password do not match any user in this service"

  # Edge case: checkout dengan cart kosong
  Scenario: Attempt checkout with an empty cart
    Given user is on the SauceDemo login page
    When user logs in with username "standard_user" and password "secret_sauce"
    Then user should see the products page
    When user goes to the cart page without adding any item
    And user proceeds to checkout
    Then user should not be able to complete the checkout

  # Edge case: data checkout form tidak lengkap
  Scenario: Checkout fails with incomplete form data
    Given user is on the SauceDemo login page
    When user logs in with username "standard_user" and password "secret_sauce"
    And user adds "Sauce Labs Backpack" to the cart
    And user proceeds to checkout
    And user fills in checkout information with first name "", last name "Doe", and postal code "12345"
    And user completes the checkout
    Then user should see an error message "Error: First Name is required"
