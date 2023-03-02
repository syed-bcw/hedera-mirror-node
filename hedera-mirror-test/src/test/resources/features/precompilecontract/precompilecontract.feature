@contractbase @fullsuite
Feature: Precompile Contract Base Coverage Feature

#    @critical @release @acceptance
    @precompile
    Scenario Outline: Validate Precompile Contract
        Given I successfully create a precompile contract from contract bytes
        Given I successfully create a fungible token for precompile contract tests
        Given I successfully create a non fungible token for precompile contract tests
        Then Check if fungible token is token
        Then Check if non fungible token is token
        Then Check if fungible token is frozen
        Then Check if non fungible token is frozen
        Then Check if fungible token is kyc granted
        Then Check if non fungible token is kyc granted
        Then Get token default freeze of fungible token
        Then Get token default freeze of non fungible token
        Then Get token default kyc of fungible token
        Then Get token default kyc of non fungible token
        Examples:
            | httpStatusCode |
            | 200            |
