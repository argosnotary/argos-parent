#
# Argos Notary - A new way to secure the Software Supply Chain
#
# Copyright (C) 2019 - 2020 Rabobank Nederland
# Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

@ignore
Feature: create a valid layout

  Background:
    * url karate.properties['server.baseurl']
    * def layoutPath = '/api/supplychain/'+ __arg.supplyChainId + '/layout'
    * def layoutToBeSigned = read(__arg.json)
    * def keyNumber = __arg.keyNumber
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyPair = defaultTestData.personalAccounts['default-pa'+keyNumber]

  Scenario: store layout with valid specifications should return a 201
    * def signedLayout = call read('classpath:feature/layout/sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:#(keyNumber)}
    * configure headers = call read('classpath:headers.js') { token: #(keyPair.token)}
    Given path layoutPath
    And request signedLayout.response
    When method POST
    Then status 201