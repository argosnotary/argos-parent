/*
 * Copyright (C) 2019 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.domain.verification;

public interface Verification {

    enum Priority {LAYOUT_METABLOCK_SIGNATURE, LINK_METABLOCK_SIGNATURE, BUILDSTEPS_COMPLETED, EXPECTED_COMMAND, REQUIRED_NUMBER_OF_LINKS, STEP_AUTHORIZED_KEYID, LAYOUT_AUTHORIZED_KEYID, RULES}

    Priority getPriority();

    VerificationRunResult verify(VerificationContext context);

}
