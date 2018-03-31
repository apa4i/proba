Feature: Successful searching

  Scenario Outline: Successful search in progress website and found a result
    Given browser "<browser>"
    And website loaded this address: "https://progressbg.net"
    When I execute a search for "automation"
    Then I should expect there is a result

    Examples:
      | browser |
      | chrome  |
      | firefox |