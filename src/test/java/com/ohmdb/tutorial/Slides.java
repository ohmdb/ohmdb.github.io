package com.ohmdb.tutorial;

import java.util.HashMap;
import java.util.Map;

import com.ohmdb.api.CustomIndex;
import com.ohmdb.api.Ids;
import com.ohmdb.api.ManyToMany;
import com.ohmdb.api.ManyToOne;
import com.ohmdb.api.Mapper;
import com.ohmdb.api.Ohm;
import com.ohmdb.api.Db;
import com.ohmdb.api.OneToMany;
import com.ohmdb.api.OneToOne;
import com.ohmdb.api.Parameter;
import com.ohmdb.api.Search;
import com.ohmdb.api.Table;
import com.ohmdb.api.Transaction;
import com.ohmdb.api.TransactionException;
import com.ohmdb.api.TransactionListener;
import com.ohmdb.api.Transformer;
import com.ohmdb.api.Trigger;
import com.ohmdb.api.TriggerAction;

public class Slides {

	static long id1 = 0, id2 = 0, id3 = 0, id4 = 0, id5 = 0, pid1 = 0;

	public static void main(String[] args) throws TransactionException {
		
/*
 * Let's create a database!|
 */

Db db = Ohm.db("socialnet.db");

Table<User> users = db.table(User.class);
Table<Post> posts = db.table(Post.class);
Table<Wall> walls = db.table(Wall.class);

users.insert(new User("abastien", "Adele Bastien", 28, "Paris"));
users.insert(new User("jd357", "John Dean", 30, "New York"));
posts.insert(new Post("OhmDB is so irresistable ;) "));

users.print();

int size = users.size();

users.clear();

/***/

/*
 * CRUD|
 */

User u1 = users.get(id1);
User[] userlist = users.getAll(id2, id3, id5);

users.set(id1, "username", "dean.john");

User new_user = new User("jack.green", "Jack Green", 30, "New York");
users.update(id4, new_user);

Map<String, Object> map = new HashMap<String, Object>();
map.put("username", "xman_jd");
map.put("age", 18);
users.update(id2, map);

users.delete(id1);

/***/

/* Indexing on username| */

users.createIndexOnNamed("username");
users.where("username", String.class).eq("tcapelo").print();

User u = users.queryHelper();

users.createIndexOn(u.username);

users.where(u.username).eq("tcapelo").print();

/***/

/* Indexing on lowercase| */

users.createIndexOn(u.username, new Transformer<String>() {
	@Override
	public String transform(String value) {
		return value != null ? value.toLowerCase() : null;
	}
});

/***/

/* Searching| */

users.createIndexOn(u.age);
users.createIndexOn(u.city);
users.createIndexOn(u.fullName);

users.where(u.fullName).eq("Bella Santorini").and("age", int.class).gte(20).print();

users.where(u.username).eq("mary_NY").and(u.age).gt(20).print();

users.where(u.fullName).eq("Toni Capelo").or("age", int.class).lt(30).print();

users.where(u.fullName).eq("Toni Capelo").or(u.age).neq(30).print();

/***/

/* Retrieving results| */

users.where(u.age).gt(24).top(5).print();

users.where(u.age).lte(28).bottom(5).print();

users.where(u.city).neq("New York").range(2, 5).print();

User usr = users.where(u.fullName).eq("Toni Capel").getIfExists();

usr = users.where(u.city).eq("Milano").getOnly();

/***/

/* Parameterized queries| */

Parameter<Integer> age = db.param("age", Integer.class);

long[] idss = users.where(u.age).eq(age).bind(age.as(28)).ids();

/***/

/* Complex indexing| */

CustomIndex<User, String> username_age = users.index(new Mapper<User, String>() {
	@Override
	public String map(User u) {
		return u.username + "-" + u.age;
	}
}, u.username, u.age);

users.where(username_age).eq("tcapelo-28").print();
users.where(username_age).eq("tcapelo-28").or(username_age).lte("tcapelo-30").print();

/* Full text search| */

Post p = posts.queryHelper();

CustomIndex<Post, String> words = posts.multiIndex(new Mapper<Post, String[]>() {
	@Override
	public String[] map(Post p) {
		return p.content.split(" ");
	}
}, p.content);

posts.where(words).eq("dude").or(words).eq("java").print();

/***/

/* Relations| */

OneToOne<User, Wall> owns = db.oneToOne(users, "owns", walls);

OneToMany<User, Post> published = db.oneToMany(users, "published", posts);

ManyToOne<Post, User> publishedBy = published.inversed();

ManyToMany<User, Post> likes = db.manyToMany(users, "likes", posts);

ManyToMany<User, User> friend = db.manyToManySymmetric(users, "friend", users);

friend.link(id3, id2);

likes.delink(id5, pid1);

/***/

/* Types of joins| */

db.join(users.all(), friend, users.all()).exists();

db.leftJoin(users.all(), owns, walls.all()).all();

db.rightJoin(users.all(), owns, walls.all()).exists();

db.fullJoin(users.all(), owns, walls.all()).all();

/***/

/*
 * Friends of user X who liked post Y|
 */

Ids<User> usrX = users.withIds(id1);
Ids<User> allfriends = users.all();
Ids<Post> postY = posts.withIds(pid1);

db.join(usrX, friend, allfriends).join(allfriends, likes, postY).all();

/***/

/*
 * Friends of user A who liked posts posted by user B|
 */

Ids<User> usrA = users.withIds(id3);
Ids<User> usrB = users.withIds(id2);
Ids<Post> all_posts = posts.all();
Ids<User> all_friends = users.all();

db.join(usrA, friend, all_friends).join(all_friends, likes, all_posts).join(all_posts, publishedBy, usrB).all();

/***/

/*
 * Friends of user C who liked post D and are older than 25|
 */

User usrs = users.queryHelper();
users.createIndexOn(usrs.age);

Ids<User> usrC = users.withIds(id1);
Search<User> older = users.where(usrs.age).gt(25);
db.join(usrC, friend, older).join(older, likes, posts.all()).all();

/***/

/*
 * Triggers|
 */

db.before(User.class).inserted().or().updated().run(new Trigger<User>() {
	@Override
	public void process(TriggerAction action, long id, User oldEntity, User newEntity) {
		newEntity.fullName = newEntity.fullName.toUpperCase();
	}
});

/***/

/*
 * Async Transactions|
 */

Transaction tx = db.startTransaction();

tx.addListener(new TransactionListener() {
	@Override
	public void onSuccess() {
	}

	@Override
	public void onError(Exception e) {
	}
});

try {
	users.delete(10);
	walls.delete(11);
	tx.commit();
} catch (Exception e) {
	tx.rollback();
}

/***/

/*
 * Blocking Transactions|
 */

Transaction tx2 = db.startTransaction();

try {
	
	users.delete(10);
	walls.delete(11);
	
	tx2.commit();
	
	tx2.sync();
	
} catch (Exception e) {
	tx2.rollback();
}
		
	}
	
}
