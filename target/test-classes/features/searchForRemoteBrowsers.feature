Feature: Successful searching

  Scenario Outline: Successful search in progress website and found a result
    Given browser "<browser>" with remote address: "<remoteWebDriver>"
    And website loaded this address: "https://progressbg.net"
    When I execute a search for "automation"
    Then I should expect there is a result

    Examples:
      | browser | remoteWebDriver              |
      | chrome  | http://localhost:5555/wd/hub |
      | firefox | http://localhost:5556/wd/hub |