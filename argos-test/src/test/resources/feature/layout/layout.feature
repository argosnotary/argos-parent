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

Feature: Layout

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}
    * def accountWithNoReadPermissions = call read('classpath:feature/account/create-personal-account.feature') {name: 'account with no read permissions person',email: 'local.permissions@ff.go'}
    * call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(accountWithNoReadPermissions.response.id),labelId: #(supplyChain.response.parentLabelId), permissions: ["LINK_ADD"]}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * def validLayout = 'classpath:testmessages/layout/valid-layout.json'
    * def accountWithTreeEditPermission = defaultTestData.personalAccounts['default-pa1']
    * configure headers = call read('classpath:headers.js') { token: #(accountWithTreeEditPermission.token)}
    * def accountWithoutTreeEditPermission = defaultTestData.personalAccounts['default-pa2']
    * def tokenWithoutTreeEditPermissions = accountWithoutTreeEditPermission.token

  Scenario: store layout with valid specifications should return a 200 and commit to auditlog
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'createOrUpdateLayout'
    And match stringResponse contains 'supplyChainId'
    And match stringResponse contains 'layout'

  Scenario: store layout with invalid specifications should return a 400 error
    Given path layoutPath
    And request read('classpath:testmessages/layout/invalid-layout.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/layout/invalid-layout-response.json')

  Scenario: store layout without authorization should return a 401 error
    * configure headers = null
    Given path layoutPath
    And header Content-Type = 'application/json'
    And request read(validLayout)
    When method POST
    Then status 401

  Scenario: store layout without TREE_EDIT permission should return a 403 error
    * configure headers = call read('classpath:headers.js') { token: #(tokenWithoutTreeEditPermissions)}
    * def layout2BSigned = read(validLayout)
    * def signedLayout = call read('classpath:feature/layout/sign-layout.feature') {json:#(layout2BSigned),keyNumber:1}
    Given path layoutPath
    And request signedLayout.response
    When method POST
    Then status 403

  Scenario: find layout with valid supplychainid should return a 200
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    Given path layoutPath
    When method GET
    Then status 200
    * def layoutId = layoutResponse.response.id
    * def expected = read('classpath:testmessages/layout/valid-layout-response.json')
    And match response contains expected

  Scenario: find layout without authorization should return a 401 error
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * configure headers = null
    Given path layoutPath
    And header Content-Type = 'application/json'
    When method GET
    Then status 401

  Scenario: find layout without READ permission should return a 403
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(accountWithNoReadPermissions.response.token)}
    Given path layoutPath
    When method GET
    Then status 403

  Scenario: update a layout should return a 200
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * def layoutToBeSigned = read('classpath:testmessages/layout/valid-update-layout.json')
    * def requestBody = call read('sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:1}
    Given path layoutPath
    And request requestBody.response
    When method POST
    Then status 201
    Given path layoutPath
    When method GET
    Then status 200
    * def expectedResponse = read('classpath:testmessages/layout/valid-update-layout-response.json')
    And match response contains expectedResponse

  Scenario: update a layout without TREE_EDIT permission should return a 403
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * def layoutToBeSigned = read('classpath:testmessages/layout/valid-update-layout.json')
    * def requestBody = call read('sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(tokenWithoutTreeEditPermissions)}
    Given path layoutPath
    And request requestBody.response
    When method POST
    Then status 403

  Scenario: update a layout without authorization should return a 401 error
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * def layoutToBeSigned = read('classpath:testmessages/layout/valid-update-layout.json')
    * def requestBody = call read('sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:1}
    * configure headers = null
    Given path layoutPath
    And request requestBody.response
    And header Content-Type = 'application/json'
    When method POST
    Then status 401

  Scenario: validate layout with invalid model specifications should return a 400 error
    Given path layoutPath+'/validate'
    And request read('classpath:testmessages/layout/invalid-layout-validation.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/layout/invalid-layout-validation-response.json')

  Scenario: validate layout with data input  should return a 400 error
    Given path layoutPath+'/validate'
    And request read('classpath:testmessages/layout/invalid-layout-data-validation.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/layout/invalid-layout-validation-data-response.json')

  Scenario: validate layout with valid specifications should return a 204
    Given path layoutPath+'/validate'
    And request read('classpath:testmessages/layout/valid-layout-validation.json')
    When method POST
    Then status 204

  Scenario: create ApprovalConfiguration should return a 201
    * def response = call read('create-approval-config.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1,layoutPath:#(layoutPath)}

  Scenario: create ApprovalConfiguration with repeated posts should return a 200
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    Given path layoutPath+'/approvalconfig'
    And request read('classpath:testmessages/layout/approval-config-create-multiple.json')
    When method POST
    Then status 200
    Given path layoutPath+'/approvalconfig'
    And request read('classpath:testmessages/layout/approval-config-create-multiple.json')
    When method POST
    Then status 200
    And match response == read('classpath:testmessages/layout/approval-config-create-multiple-response.json')

  Scenario: create ApprovalConfiguration without TREE_EDIT permission should return a 403
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(tokenWithoutTreeEditPermissions)}
    Given path layoutPath+'/approvalconfig'
    And request read('classpath:testmessages/layout/approval-config-create-request.json')
    When method POST
    Then status 403

  Scenario: get approvalConfigurations should return a 200
    * def createcall = call read('create-approval-config.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1,layoutPath:#(layoutPath)}
    Given path layoutPath+'/approvalconfig/'
    When method GET
    Then status 200
    And match response == read('classpath:testmessages/layout/approval-configs-response.json')

  Scenario: get approvalConfigurations with no READ permission should return a 403
    * def createcall = call read('create-approval-config.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1,layoutPath:#(layoutPath)}
    * configure headers = call read('classpath:headers.js') { token: #(accountWithNoReadPermissions.response.token)}
    Given path layoutPath+'/approvalconfig/'
    When method GET
    Then status 403


  Scenario: get personal approvalConfigurations should return a 200
    * def createcall = call read('create-approval-config.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1,layoutPath:#(layoutPath)}
    Given path layoutPath+'/approvalconfig'
    And request read('classpath:testmessages/layout/approval-config-create-multiple.json')
    When method POST
    Then status 200
    * configure headers = call read('classpath:headers.js') { token: #(accountWithTreeEditPermission.token)}
    Given path layoutPath+'/approvalconfig/me'
    When method GET
    Then status 200
    And match response == read('classpath:testmessages/layout/approval-configs-response-me.json')

  Scenario: get personal approvalConfigurations without LINK_ADD permission should return a 403
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(tokenWithoutTreeEditPermissions)}
    Given path layoutPath+'/approvalconfig/me'
    When method GET
    Then status 403

  Scenario: create release configuration should return 200
    Given path layoutPath+'/releaseconfig'
    * def releaseConfig = { artifactCollectorSpecifications: [{name: "xldeploy", type: "XLDEPLOY", uri: "https://localhost:8888", context: {applicationName: "appname"}}]}
    And request releaseConfig
    When method POST
    Then status 200
    And match response == releaseConfig

  Scenario: create release configuration without TREE_EDIT permission should return 403
    Given path layoutPath+'/releaseconfig'
    * def releaseConfig = { artifactCollectorSpecifications: [{name: "xldeploy", type: "XLDEPLOY", uri: "https://localhost:8888", context: {applicationName: "appname"}}]}
    * configure headers = call read('classpath:headers.js') { token: #(tokenWithoutTreeEditPermissions)}
    And request releaseConfig
    When method POST
    Then status 403

  Scenario: get release configuration should return 200
    Given path layoutPath+'/releaseconfig'
    When method GET
    Then status 404
    Given path layoutPath+'/releaseconfig'
    * def releaseConfig = { artifactCollectorSpecifications: [{name: "xldeploy", type: "XLDEPLOY", uri: "https://localhost:8888", context: {applicationName: "appname"}}]}
    And request releaseConfig
    When method POST
    Then status 200
    Given path layoutPath+'/releaseconfig'
    When method GET
    Then status 200
    And match response == releaseConfig

  Scenario: get release configurations with no READ permission should return a 403
    Given path layoutPath+'/releaseconfig'
    * def releaseConfig = { artifactCollectorSpecifications: [{name: "xldeploy", type: "XLDEPLOY", uri: "https://localhost:8888", context: {applicationName: "appname"}}]}
    And request releaseConfig
    When method POST
    Then status 200
    * configure headers = call read('classpath:headers.js') { token: #(accountWithNoReadPermissions.response.token)}
    Given path layoutPath+'/releaseconfig'
    When method GET
    Then status 403