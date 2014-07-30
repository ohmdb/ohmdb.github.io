package com.ohmdb.web;

/*
 * #%L
 * ohmdb-demo
 * %%
 * Copyright (C) 2013 - 2014 Nikolche Mihajlovski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ohmdb.api.Db;
import com.ohmdb.api.Ids;
import com.ohmdb.api.Join;
import com.ohmdb.api.ManyToMany;
import com.ohmdb.api.Ohm;
import com.ohmdb.api.Table;
import com.ohmdb.util.Measure;
import com.ohmdb.util.U;

public class BenchmarkTest {

	private static final Random RND = new Random();

	private static final String DB_FILENAME = "/tmp/benchmark.db";

	private final Map<String, List<Long>> measurements = new HashMap<String, List<Long>>();

	@Test
	public void testBenchmark() {
		benchmark(1);
		benchmark(5);
		benchmark(10);
		benchmark(50);
		benchmark(100);

		Gson gson = new GsonBuilder().create();
		System.out.println(gson.toJson(measurements));
	}

	private void benchmark(int factor) {
		int usersN = 100 * factor;
		int friendsN = 20;

		System.out.println("Benchmark: " + usersN + " users, each has " + friendsN + " friends in average\n");

		U.delete(DB_FILENAME);

		insert(usersN);
		update(usersN);
		set(usersN);

		long[] all = makeFriends(usersN, friendsN);

		Db db = Ohm.db(DB_FILENAME);

		joinNth(db, usersN, all, 1);
		joinNth(db, usersN, all, 2);
		joinNth(db, usersN, all, 3);
		joinNth(db, usersN, all, 4);

		db.shutdown();
	}

	private void insert(int usersN) {
		Db db = Ohm.db(DB_FILENAME);

		Table<DemoUser> users = db.table(DemoUser.class);

		Measure.start(usersN);

		DemoUser user = new DemoUser();

		for (int i = 0; i < usersN; i++) {
			user.setUsername("user" + i);
			user.setAge(i % 100);
			users.insert(user);
		}

		db.shutdown();

		finish("insert");
		System.out.println();
	}

	private void update(int usersN) {
		Db db = Ohm.db(DB_FILENAME);

		Table<DemoUser> users = db.table(DemoUser.class);

		Measure.start(usersN);

		DemoUser user = new DemoUser();

		for (int i = 0; i < usersN; i++) {
			user.setUsername("theuser" + i);
			user.setAge(i % 1000);
			users.update(i, user);
		}

		db.shutdown();

		finish("update");
		System.out.println();
	}

	private void set(int usersN) {
		Db db = Ohm.db(DB_FILENAME);

		Table<DemoUser> users = db.table(DemoUser.class);

		Measure.start(usersN);

		for (int i = 0; i < usersN; i++) {
			users.set(i, "age", 123);
		}

		db.shutdown();

		finish("set");
		System.out.println();
	}

	private void joinNth(Db db, int usersN, long[] all, int n) {
		ManyToMany<Object, Object> fr = db.manyToManySymmetric(null, "friends", null);

		Ids<Object> x = db.all(all);
		Ids<Object> y = db.all(all);
		Ids<Object> z = db.all(all);

		int total = 100;

		Measure.start(total);

		int connN = 0;

		for (int i = 0; i < total; i++) {
			long a = RND.nextInt(usersN);
			long b = RND.nextInt(usersN);

			Join join;
			switch (n) {
			case 1:
				join = db.join(db.ids(a), fr, db.ids(b));
				break;

			case 2:
				join = db.join(db.ids(a), fr, x).join(x, fr, db.ids(b));
				break;

			case 3:
				join = db.join(db.ids(a), fr, x).join(x, fr, y).join(y, fr, db.ids(b));
				break;

			case 4:
				join = db.join(db.ids(a), fr, x).join(x, fr, y).join(y, fr, z).join(z, fr, db.ids(b));
				break;

			default:
				throw new IllegalArgumentException("Wrong N!");
			}

			boolean connected = join.exists();

			if (connected) {
				connN++;
			}
		}

		System.out.println(connN + " of " + total + " random couples were connected at rank " + n);

		finish(n + " join(s)");
		System.out.println();
	}

	private long[] makeFriends(int usersN, int friendsN) {
		Db db = Ohm.db(DB_FILENAME);
		ManyToMany<Object, Object> fr = db.manyToManySymmetric(null, "friends", null);

		Measure.start(usersN * friendsN);

		long[] all = new long[usersN];
		for (int i = 0; i < usersN; i++) {
			all[i] = i;
			for (int j = 0; j < friendsN; j++) {
				fr.link(i, RND.nextInt(usersN));
			}
		}

		db.shutdown();

		finish("link");
		System.out.println();
		return all;
	}

	private void finish(String info) {
		String infoN = info + "N";

		List<Long> measure = measurements.get(info);
		List<Long> measureN = measurements.get(infoN);

		if (measure == null) {
			measure = new ArrayList<Long>();
			measureN = new ArrayList<Long>();
			measurements.put(info, measure);
			measurements.put(infoN, measureN);
		}

		measure.add(Measure.finish(info));
		measureN.add((long) Measure.getCount());
	}

}
