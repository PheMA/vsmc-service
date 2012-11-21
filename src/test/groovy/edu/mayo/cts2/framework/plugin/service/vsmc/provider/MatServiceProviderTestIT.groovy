package edu.mayo.cts2.framework.plugin.service.vsmc.provider

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test

import edu.mayo.cts2.framework.plugin.service.vsmc.test.AbstractTestITBase
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionReadService


class MatServiceProviderTestIT extends AbstractTestITBase {

	@Resource
	def MatServiceProvider provider

	@Test
	void TestSetUp() {
		assertNotNull provider
	}

	@Test
	void TestGetEntityRead() {
		assertNotNull provider.getService(EntityDescriptionReadService.class)
	}

}
