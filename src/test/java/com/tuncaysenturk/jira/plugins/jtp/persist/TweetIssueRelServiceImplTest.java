package com.tuncaysenturk.jira.plugins.jtp.persist;

import static org.junit.Assert.*;
import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.tuncaysenturk.jira.plugins.jtp.entity.TweetIssueRel;

@RunWith(ActiveObjectsJUnitRunner.class)
public class TweetIssueRelServiceImplTest {

	private EntityManager entityManager;

	private ActiveObjects ao;

	private TweetIssueRelServiceImpl tweetIssueService;

	@Before
	public void setUp() throws Exception {
		assertNotNull(entityManager);
		ao = new TestActiveObjects(entityManager);
		tweetIssueService = new TweetIssueRelServiceImpl(new TestActiveObjects(
				entityManager));
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAdd() throws Exception {
		final Long issueId = 12345L;
		final Long statusId = 67890L;
		ao.migrate(TweetIssueRel.class);

		assertEquals(0, ao.find(TweetIssueRel.class).length);

		tweetIssueService.persistRelation(issueId, statusId);

		ao.flushAll(); // (3) clear all caches

		final TweetIssueRel[] relations = ao.find(TweetIssueRel.class);
		assertEquals(1, relations.length);
		assertEquals(issueId, relations[0].getIssueId());
		assertEquals(statusId, relations[0].getTweetStatusId());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindByStatusId() throws Exception {
		final Long issueId = 12345L;
		final Long statusId = 67890L;
		ao.migrate(TweetIssueRel.class);
		 
        assertNull(tweetIssueService.findTweetIssueRelByTweetStatusId(statusId));
 
        final TweetIssueRel rel = ao.create(TweetIssueRel.class);
        rel.setIssueId(issueId);
        rel.setTweetStatusId(statusId);
        rel.save();
 
        ao.flushAll();
 
        final TweetIssueRel relFound = tweetIssueService.findTweetIssueRelByTweetStatusId(statusId);
        assertNotNull(relFound);
        assertEquals(rel.getID(), relFound.getID());
        assertEquals(rel.getIssueId(), relFound.getIssueId());
	}
}
