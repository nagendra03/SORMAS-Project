/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/

package de.symeda.sormas.ui;

import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.io.input.BOMInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.opencsv.CSVReader;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.caze.caseimport.CaseImportFacade;
import de.symeda.sormas.api.event.EventFacade;
import de.symeda.sormas.api.event.EventParticipantFacade;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.immunization.ImmunizationFacade;
import de.symeda.sormas.api.infrastructure.community.CommunityFacade;
import de.symeda.sormas.api.infrastructure.country.CountryFacade;
import de.symeda.sormas.api.infrastructure.district.DistrictFacade;
import de.symeda.sormas.api.infrastructure.facility.FacilityFacade;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryFacade;
import de.symeda.sormas.api.infrastructure.region.RegionFacade;
import de.symeda.sormas.api.person.PersonFacade;
import de.symeda.sormas.api.sample.PathogenTestFacade;
import de.symeda.sormas.api.sample.SampleFacade;
import de.symeda.sormas.api.travelentry.TravelEntryFacade;
import de.symeda.sormas.api.user.DefaultUserRole;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.utils.CSVUtils;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.caze.caseimport.CaseImportFacadeEjb.CaseImportFacadeEjbLocal;
import de.symeda.sormas.backend.contact.ContactFacadeEjb.ContactFacadeEjbLocal;
import de.symeda.sormas.backend.disease.DiseaseConfiguration;
import de.symeda.sormas.backend.disease.DiseaseConfigurationService;
import de.symeda.sormas.backend.event.EventFacadeEjb.EventFacadeEjbLocal;
import de.symeda.sormas.backend.event.EventParticipantFacadeEjb.EventParticipantFacadeEjbLocal;
import de.symeda.sormas.backend.immunization.ImmunizationFacadeEjb;
import de.symeda.sormas.backend.infrastructure.community.CommunityFacadeEjb.CommunityFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.country.CountryFacadeEjb;
import de.symeda.sormas.backend.infrastructure.district.DistrictFacadeEjb.DistrictFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.facility.FacilityFacadeEjb.FacilityFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.pointofentry.PointOfEntryFacadeEjb.PointOfEntryFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb.RegionFacadeEjbLocal;
import de.symeda.sormas.backend.person.PersonFacadeEjb.PersonFacadeEjbLocal;
import de.symeda.sormas.backend.sample.PathogenTestFacadeEjb;
import de.symeda.sormas.backend.sample.SampleFacadeEjb;
import de.symeda.sormas.backend.travelentry.TravelEntryFacadeEjb;
import de.symeda.sormas.backend.user.CurrentUserService;
import info.novatec.beantest.api.BaseBeanTest;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class AbstractBeanTest extends BaseBeanTest {

	protected final TestDataCreator creator = new TestDataCreator();

	@BeforeEach
	public void beforeEach() {
		// so we can override init
		init();
	}
	/**
	 * Resets mocks to their initial state so that mock configurations are not
	 * shared between tests.
	 */
	public void init() {

		super.initilaize();

		MockProducer.wireMocks();
		initH2Functions();
		// this is used to provide the current user to the ADO Listener taking care of updating the last change user
		System.setProperty("java.naming.factory.initial", MockProducer.class.getCanonicalName());
		I18nProperties.setUserLanguage(Language.EN);

		createTestUser();

		// see CurrentUserService.hasRight
//		when(MockProducer.getSessionContext().isCallerInRole(any(String.class))).thenAnswer(invocationOnMock -> {
//			String role = invocationOnMock.getArgument(0);
//			UserRight userRight = UserRight.valueOf(role);
//			return getCurrentUserService().getCurrentUser()
//				.getUserRoles()
//				.stream()
//				.anyMatch(userRole -> userRole.getUserRights().contains(userRight));
//		});
		createDiseaseConfigurations();
	}

	@AfterEach
	public void cleanUp() {

		super.cleanUp();
		MockProducer.resetMocks();
	}

	private void createTestUser() {
		initTestUser();

		when(MockProducer.getPrincipal().getName()).thenReturn("admin");

		UserDto user = FacadeProvider.getUserFacade().getByUserName("admin");

		user.setChangeDate(new Date());
		user.setLanguage(Language.EN);
		user.getUserRoles().add(creator.getUserRoleReference(DefaultUserRole.ADMIN));
		user.getUserRoles().add(creator.getUserRoleReference(DefaultUserRole.NATIONAL_USER));

		FacadeProvider.getUserFacade().saveUser(user, false);
	}

	private void initH2Functions() {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		Query nativeQuery = em.createNativeQuery("CREATE ALIAS similarity FOR \"de.symeda.sormas.ui.H2Function.similarity\"");
		nativeQuery.executeUpdate();
		nativeQuery = em.createNativeQuery("CREATE ALIAS similarity_operator FOR \"de.symeda.sormas.ui.H2Function.similarity_operator\"");
		nativeQuery.executeUpdate();
		nativeQuery = em.createNativeQuery("CREATE ALIAS set_limit FOR \"de.symeda.sormas.ui.H2Function.set_limit\"");
		nativeQuery.executeUpdate();
		nativeQuery = em.createNativeQuery("CREATE ALIAS date FOR \"de.symeda.sormas.ui.H2Function.date\"");
		nativeQuery.executeUpdate();
		em.getTransaction().commit();
	}

	private void createDiseaseConfigurations() {
		List<DiseaseConfiguration> diseaseConfigurations = getDiseaseConfigurationService().getAll();
		List<Disease> configuredDiseases = diseaseConfigurations.stream().map(c -> c.getDisease()).collect(Collectors.toList());
		Arrays.stream(Disease.values()).filter(d -> !configuredDiseases.contains(d)).forEach(d -> {
			DiseaseConfiguration configuration = DiseaseConfiguration.build(d);
			getDiseaseConfigurationService().ensurePersisted(configuration);
		});
	}

	private void initTestUser() {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();

		Query nativeQuery = em.createNativeQuery(
			"INSERT INTO location (id, uuid, creationdate, changedate) values (0, '" + DataHelper.createUuid() + "', now(), now())");
		nativeQuery.executeUpdate();

		nativeQuery = em.createNativeQuery(
			"INSERT INTO users (id, uuid, creationdate, changedate, username, firstName, lastName, password, seed, active, hasconsentedtogdpr, jurisdictionlevel, address_id) values (0, '"
				+ DataHelper.createUuid() + "', now(), now(), 'admin', 'ad', 'min', 'testpass', 'seed', true, true, 'NONE', 0)");
		nativeQuery.executeUpdate();

		nativeQuery = em.createNativeQuery(
			"INSERT INTO userroles (id, uuid, creationdate, changedate, caption, jurisdictionLevel, enabled, porthealthuser, hasassociateddistrictuser, hasoptionalhealthfacility) values (0, '"
				+ DataHelper.createUuid() + "', now(), now(), 'Admin init', 'NONE', false, false, false, false)");
		nativeQuery.executeUpdate();

		nativeQuery = em.createNativeQuery("INSERT INTO userroles_userrights (userrole_id, userright) values (0, 'USER_EDIT')");
		nativeQuery.executeUpdate();

		nativeQuery = em.createNativeQuery("INSERT INTO users_userroles (user_id, userrole_id) values (0, 0)");
		nativeQuery.executeUpdate();

		em.getTransaction().commit();
	}

	public CurrentUserService getCurrentUserService() {
		return getBean(CurrentUserService.class);
	}

	public PersonFacade getPersonFacade() {
		return getBean(PersonFacadeEjbLocal.class);
	}

	public CaseFacadeEjbLocal getCaseFacade() {
		return getBean(CaseFacadeEjbLocal.class);
	}

	public ImmunizationFacade getImmunizationFacade() {
		return getBean(ImmunizationFacadeEjb.ImmunizationFacadeEjbLocal.class);
	}

	public TravelEntryFacade getTravelEntryFacade() {
		return getBean(TravelEntryFacadeEjb.TravelEntryFacadeEjbLocal.class);
	}

	public ContactFacadeEjbLocal getContactFacade() {
		return getBean(ContactFacadeEjbLocal.class);
	}

	public CaseImportFacade getCaseImportFacade() {
		return getBean(CaseImportFacadeEjbLocal.class);
	}

	public EventFacade getEventFacade() {
		return getBean(EventFacadeEjbLocal.class);
	}

	public EventParticipantFacade getEventParticipantFacade() {
		return getBean(EventParticipantFacadeEjbLocal.class);
	}

	public CountryFacade getCountryFacade() {
		return getBean(CountryFacadeEjb.CountryFacadeEjbLocal.class);
	}

	public RegionFacade getRegionFacade() {
		return getBean(RegionFacadeEjbLocal.class);
	}

	public DistrictFacade getDistrictFacade() {
		return getBean(DistrictFacadeEjbLocal.class);
	}

	public CommunityFacade getCommunityFacade() {
		return getBean(CommunityFacadeEjbLocal.class);
	}

	public FacilityFacade getFacilityFacade() {
		return getBean(FacilityFacadeEjbLocal.class);
	}

	public PointOfEntryFacade getPointOfEntryFacade() {
		return getBean(PointOfEntryFacadeEjbLocal.class);
	}

	public EntityManager getEntityManager() {
		return getBean(EntityManagerWrapper.class).getEntityManager();
	}

	public DiseaseConfigurationService getDiseaseConfigurationService() {
		return getBean(DiseaseConfigurationService.class);
	}

	public PathogenTestFacade getPathogenTestFacade() {
		return getBean(PathogenTestFacadeEjb.PathogenTestFacadeEjbLocal.class);
	}

	public SampleFacade getSampleFacade() {
		return getBean(SampleFacadeEjb.SampleFacadeEjbLocal.class);
	}

	public CSVReader getCsvReader(InputStream inputStream) {
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		BOMInputStream bomInputStream = new BOMInputStream(inputStream);
		Reader reader = new InputStreamReader(bomInputStream, decoder);
		BufferedReader bufferedReader = new BufferedReader(reader);
		return CSVUtils.createCSVReader(bufferedReader, ',');
	}

	protected void loginWith(UserDto user) {
		when(MockProducer.getPrincipal().getName()).thenReturn(user.getUserName());
	}
}
