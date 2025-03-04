/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2022 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.ui.externalmessage.labmessage.processing;

import de.symeda.sormas.api.sample.SampleDto;

public class PickOrCreateSampleResult {

	private SampleDto sample;

	private boolean newSample;

	public SampleDto getSample() {
		return sample;
	}

	public void setSample(SampleDto sample) {
		this.sample = sample;
	}

	public boolean isSelectedSample() {
		return sample != null;
	}

	public boolean isNewSample() {
		return newSample;
	}

	public void setNewSample(boolean newSample) {
		this.newSample = newSample;
	}
}
