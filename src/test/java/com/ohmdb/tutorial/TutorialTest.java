package com.ohmdb.tutorial;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.ohmdb.api.CustomIndex;
import com.ohmdb.api.Db;
import com.ohmdb.api.Ids;
import com.ohmdb.api.JoinResult;
import com.ohmdb.api.Links;
import com.ohmdb.api.ManyToMany;
import com.ohmdb.api.ManyToOne;
import com.ohmdb.api.Mapper;
import com.ohmdb.api.Ohm;
import com.ohmdb.api.OneToMany;
import com.ohmdb.api.OneToOne;
import com.ohmdb.api.Parameter;
import com.ohmdb.api.Search;
import com.ohmdb.api.Table;
import com.ohmdb.api.Transaction;
import com.ohmdb.api.Transformer;

public class TutorialTest {

	@Test
	public void tutorial() {

		/* BEGIN */
		
		new File("socialnet.db").delete();

		/*
		 * Creating the database|Let's "make" a simple social network
		 * application. The first thing we need is a database, so let's create
		 * one.|
		 */

		Db db = Ohm.db("socialnet.db");

		System.out.println("/***/");

		/*
		 * Creating a table|Now let's create your first table - table of users.|
		 */

		Table<User> users = db.table(User.class);

		System.out.println("/***/");

		/*
		 * Insert records|Next thing you need to do is to insert some users into
		 * the table using ^insert^ method.|
		 */

		User user0 = new User("ann.bernard", "Ann Bernard", 22, "Paris");
		long id0 = users.insert(user0);

		User user1 = new User("jd357", "John Dean", 30, "New York");
		long id1 = users.insert(user1);

		User user2 = new User("abastien", "Adele Bastien", 28, "Paris");
		long id2 = users.insert(user2);

		long id3 = users.insert(new User("mary_NY", "Mary Williams", 32, "New York"));
		long id4 = users.insert(new User("tcapelo", "Toni Capelo", 25, "Milano"));
		long id5 = users.insert(new User("natasa_b", "Natasa Bodrova", 22, "Moscow"));
		long id6 = users.insert(new User("bella.santorini", "Bella Santorini", 24, "Milano"));
		long id7 = users.insert(new User("bob_wood", "Bob Wood", 30, "New York"));

		System.out.println("/***/");

		/*
		 * Print the table|Now you have a table filled with some users. You can
		 * print all the records from a table into the standard output using the
		 * ^print^ method. This method is useful during development and
		 * debugging.|
		 */

		users.print();

		System.out.println("/***/");

		/*
		 * Table size|The number of records in a table is retrieved using the
		 * ^size^ method.|
		 */

		debug("Users table size", users.size());

		System.out.println("/***/");

		/*
		 * Get record by ID|Fetching a record by its ID is done with the ^get^
		 * method.|
		 */

		User u1 = users.get(id1);

		debug("User with id=" + id1 + " is " + u1);

		System.out.println("/***/");

		/*
		 * Get records by IDs|Fetching records by their IDs is done with the
		 * ^getAll^ method.|
		 */

		User[] userlist = users.getAll(id1, id2, id3);

		debug("Users by ids: ", userlist);

		System.out.println("/***/");

		/*
		 * Change column value|The ^set^ method can be used to change the value
		 * of a column for a specific record.|
		 */

		debug("User before set", users.get(id2));

		users.set(id2, "username", "dean.john");

		debug("User after set", users.get(id2));

		System.out.println("/***/");

		/*
		 * Update record|The ^update^ method can be used to update a record in a
		 * table. All the columns values will be updated with the new values
		 * from the specified bean. The target record ID is retrieved from the
		 * "id" field/property of the specified bean.|
		 */

		User usr1 = users.get(id1);

		debug("Before update", usr1);

		usr1.username = "adele73";
		users.update(usr1);

		debug("After update", users.get(id1));

		System.out.println("/***/");

		/*
		 * Update record by specifying ID|The ^update^ method can also be used
		 * to update a record in a table, by specifying its ID. All the columns
		 * values will be updated with the new values from the specified bean.|
		 */

		debug("Before update", users.get(id2));

		User jack = new User("jack.green", "Jack Green", 30, "New York");
		users.update(id2, jack);

		debug("After update", users.get(id2));

		System.out.println("/***/");

		/*
		 * Update record from a map|A record can be also updated by specifying
		 * its new column values in a map.|
		 */
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("username", "xman_jd");
		map.put("age", 18);

		debug("Before update", users.get(id2));

		users.update(id2, map);

		debug("After update", users.get(id2));

		System.out.println("/***/");

		/*
		 * Delete record|You can delete a record by its ID using the ^delete^
		 * method:|
		 */

		debug("Users before delete", users.size());

		users.delete(id6);

		debug("Users after delete", users.size());

		System.out.println("/***/");

		/*
		 * Get all records' IDs|The ^ids^ method retrieves the IDs of all
		 * records in the table.|
		 */

		long[] allUserIds = users.ids();

		debug("All IDs of users", allUserIds);

		System.out.println("/***/");

		/*
		 * Searching and indexing| Let's make a simple search, for example to
		 * find all users that have "tcapelo" as their username.
		 * 
		 * This can be done in a couple of ways. Let's examine the simplest way
		 * first. An index on column "username" is created using method
		 * ^createIndexOnNamed^. Afterwards, the search is conducted using the
		 * ^where^ method, where you can state by what column the search to be
		 * made on.
		 * 
		 * This method gives a criteria as result, which could be "equals"
		 * represented with ^eq^ method, "greater that" represented with ^gt^
		 * method, "greater than or equal" represented with ^gte^ method,
		 * "less than" represented with ^lt^ method, "less than or equal"
		 * represented with ^lte^ and "not equal" represented with ^neq^ method.
		 * 
		 * In this case, ^eq^ method will do the trick. Nevertheless, specifying
		 * the column name as string is error-prone, so you have to be careful
		 * not to make a naive spelling mistake, or take a look at the type-safe
		 * approach described next.|
		 */
		users.createIndexOnNamed("username");
		users.where("username", String.class).eq("tcapelo").print();

		System.out.println("/***/");

		/*
		 * Type-safe searching and indexing|The type-safe way to make a search
		 * is by using ^queryHelper^ method. It creates a special kind of query
		 * helper object, in this case - User, which can be used afterwards in
		 * the search.
		 * 
		 * Using the query helper, an index is created with the ^createIndexOn^
		 * method. Finally the search is conducted using the ^where^ method with
		 * the column expressed in a type-safe way with the query helper.
		 * 
		 * This way of searching/indexing is much safer because there is no
		 * place for misspelling, or making an error on the type of the column.
		 * It also helps a lot when refactoring (e.g. renaming a column).|
		 */

		User u = users.queryHelper();

		users.createIndexOn(u.username);

		users.where(u.username).eq("tcapelo").print();

		System.out.println("/***/");

		/*
		 * Query helpers unmystified|To explain how the query helpers work,
		 * let's do some "magic". The query helpers contain encoding information
		 * in their field/property values, which is used to retrieve the column
		 * name.|
		 */

		debug(u.username + " => " + users.nameOf(u.username));
		debug(u.age + " => " + users.nameOf(u.age));

		System.out.println("/***/");

		/*
		 * Custom searching and indexing|Sometimes a more custom search is
		 * needed. For example, what if case-insensitive search is required? The
		 * method ^createIndexOn^ also can be used with a second argument of
		 * type Transformer, which serves to transform both the indexed values
		 * and the searched values.
		 * 
		 * In this case, the lower-case value of column `username` is used for
		 * the index. When a search is conducted, the lower-case version of the
		 * searched value is matched in the index.
		 * 
		 * Additionally, the method ^createIndexOnNamed^ can be used with
		 * transformer as a second argument too.|
		 */

		users.createIndexOn(u.username, new Transformer<String>() {
			@Override
			public String transform(String value) {
				return value != null ? value.toLowerCase() : null;
			}
		});

		System.out.println("/***/");

		/*
		 * Searching by multiple criteria|A conjunction and disjunction of
		 * search criteria is constructed using the ^and^ and ^or^ methods.|
		 */

		users.createIndexOn(u.age);
		users.createIndexOn(u.city);
		users.createIndexOn(u.fullName);

		users.where(u.fullName).eq("Bella Santorini").and("age", int.class).gte(20).print();

		users.where(u.username).eq("mary_NY").and(u.age).gt(20).print();

		users.where(u.fullName).eq("Toni Capelo").or("age", int.class).lt(30).print();

		users.where(u.fullName).eq("Toni Capelo").or(u.age).neq(30).print();

		System.out.println("/***/");

		/*
		 * Paging|When there are a lot of records as a result of a search, it is
		 * useful to be able to see just a part of the results. The ^top^,
		 * ^bottom^ and ^range^ methods can be used to get the search results in
		 * a specified range.|
		 */

		debug("Top 5");
		users.where(u.age).gt(24).top(5).print();

		debug("Bottom 3");
		users.where(u.age).lte(28).bottom(3).print();

		debug("2nd to 5th result of the search:");
		users.where(u.city).neq("New York").range(2, 5).print();

		System.out.println("/***/");

		/*
		 * Get optional result|You can use the ^getIfExists^ method in a search
		 * to get a record if it exists. If the record does not exist, null is
		 * returned.|
		 */

		User usr = users.where(u.fullName).eq("Toni Capelo").getIfExists();

		if (usr != null) {
			debug("There exists a user named Toni Capelo and here it is", usr);
		} else {
			debug("There is no user named Toni Capelo and the result of the search is", usr);
		}

		System.out.println("/***/");

		/*
		 * Get exactly one result|You can use ^getOnly^ method in a search to
		 * get the only one expected result that satisfies the criteria, or
		 * throw exception otherwise.|
		 */

		try {
			usr = users.where(u.city).eq("Milano").getOnly();

			debug("The only one user living in Milano", usr);
		} catch (Exception e) {
			debug("There is none or more than one user living in Milano!");
		}

		System.out.println("/***/");

		/*
		 * Parameterized queries|Parameterized queries can be constructed and
		 * later bound with values:|
		 */

		Parameter<Integer> age = db.param("age", Integer.class);

		Search<User> search = users.where(u.age).eq(age);

		debug("Younger than 28");
		search.bind(age.as(28)).print();

		debug("Younger than 23");
		search.bind(age.as(23)).print();

		System.out.println("/***/");

		/*
		 * Custom calculated index| Often more complex indexing is needed. What
		 * if it you have to search on a combination of a columns of the
		 * records? You can create a custom index calling the ^index^ method
		 * with a ^Mapper^ object which calculates the custom index value from
		 * the column values of a given record, and the columns that are used in
		 * the calculation of the index value.|
		 */

		CustomIndex<User, String> name_age = users.index(new Mapper<User, String>() {
			@Override
			public String map(User u) {
				return u.username + "_" + u.age;
			}
		}, u.username, u.age);

		users.where(name_age).eq("tcapelo_28").print();

		users.where(name_age).eq("tcapelo_28").or(name_age).eq("tcapelo_30").print();

		System.out.println("/***/");

		/*
		 * Multi-value indexing: full-text search|Full-text searching on a
		 * column requires indexing on each word found in the value. This is
		 * called `multi-value` indexing, because more then one value will be
		 * indexed per column per record.
		 * 
		 * The ^multiIndex^ method is called with a ^Mapper^ object (which
		 * calculates the custom index values from the column values of a given
		 * record) and the columns that are used in the calculation of the index
		 * values.|
		 */

		CustomIndex<User, String> words = users.multiIndex(new Mapper<User, String[]>() {
			@Override
			public String[] map(User u) {
				return u.fullName.split(" ");
			}
		}, u.fullName);

		users.where(words).eq("Adele").or(words).eq("Santorini").print();

		System.out.println("/***/");

		/*
		 * More tables|Now, let's proceed with the development of database for
		 * the social network application. This is a very simple social network
		 * where users have their own walls on which they or their friends can
		 * write and like posts and comments. In the following code tables
		 * `posts`, `walls` and `comments` are created.|
		 */

		Table<Post> posts = db.table(Post.class);
		Table<Wall> walls = db.table(Wall.class);
		Table<Comment> comments = db.table(Comment.class);

		System.out.println("/***/");

		/*
		 * OneToOne relations|Each user can have only one wall. This oneToOne
		 * relation is modeled using method ^oneToOne^ from our database
		 * instance db. This relation is named "owns". Analogous to that, each
		 * wall is "owned" by only one user, which is appropriately modeled into
		 * the following code by using ^inversed^ method.|
		 */

		OneToOne<User, Wall> owns = db.oneToOne(users, "owns", walls);
		OneToOne<Wall, User> ownedBy = owns.inversed();

		System.out.println("/***/");

		/*
		 * OneToMany relations|An user can make posts and write comments. There
		 * can be many posts on a wall. One user can make many posts and
		 * comments.These "oneToMany" relations are modeled by ^oneToMany^
		 * method.|
		 */

		OneToMany<Wall, Post> has = db.oneToMany(walls, "has", posts);
		OneToMany<Post, Comment> hasComment = db.oneToMany(posts, "hasC", comments);
		OneToMany<User, Post> makes = db.oneToMany(users, "makes", posts);

		System.out.println("/***/");

		/*
		 * Inverse relations|There can be many posts on one wall, many comments
		 * on one post, many posts and comments from one user. These "manyToOne"
		 * relations are modeled by ^inversed^ method on previously described
		 * "oneToMany" relations.|
		 */

		ManyToOne<Post, Wall> placedOn = has.inversed();
		ManyToOne<Comment, Post> belongsTo = hasComment.inversed();
		ManyToOne<Post, User> madeBy = makes.inversed();

		System.out.println("/***/");

		/*
		 * ManyToOne relations|Analogically on previously described relations,
		 * the relation "manyToOne" can be modeled by ^manyToOne^ method.|
		 */

		ManyToOne<Comment, User> writtenBy = db.manyToOne(comments, "writtenBy", users);
		OneToMany<User, Comment> writes = writtenBy.inversed();

		System.out.println("/***/");

		/*
		 * ManyToMany relations|Many users can make likes on many comments and
		 * posts.These "manyToMany" relations are modeled by ^manyToMany^
		 * method.|
		 */

		ManyToMany<User, Comment> likesComment = db.manyToMany(users, "likesC", comments);
		ManyToMany<User, Post> likes = db.manyToMany(users, "likes", posts);

		ManyToMany<Comment, User> commentLikedBy = likesComment.inversed();
		ManyToMany<Post, User> likedBy = likes.inversed();

		System.out.println("/***/");

		/*
		 * Symmetric relations|Each user can have many friends. That is modeled
		 * by "friend" relation between users, and because this is a symmetric
		 * relationship between entities from the same type,
		 * ^manyToManySymmetric^ method is used.|
		 */

		ManyToMany<User, User> friend = db.manyToManySymmetric(users, "friend", users);

		System.out.println("/***/");

		/*
		 * Creating relation records: use of the ^link^ method 1|Here a new wall
		 * record is created and inserted into walls table. Afterward a link is
		 * created between user1 and the wall by their ids.|
		 */

		Wall wall1 = new Wall();
		walls.insert(wall1);
		owns.link(id1, wall1.id);

		System.out.println("/***/");

		/*
		 * Creating relation records: use of the ^link^ method 2|Here the same
		 * thing is done, but on another way. The link created here is made by
		 * entities.|
		 */

		Wall wall2 = new Wall();
		walls.insert(wall2);
		owns.link(user2, wall2);

		System.out.println("/***/");

		/*
		 * First query: use of ^join^ method|Here a simple query is created:
		 * Find all users who own walls. To accomplish that, it is necessary to
		 * join two tables, users and walls by "own" relation. That is done by
		 * the ^join^ method. You can notice that here also ^any^ method is used
		 * on both tables. It's purpose is to get all user ids and all walls
		 * ids.The result from the following query is a JoinResult object, which
		 * you can see as an object containing array of links representing
		 * joins. In this case, because the join is made only on two tables,
		 * users and wall, the array has only one links object.|
		 */

		JoinResult usersWalls = db.join(users.all(), owns, walls.all()).all();
		debug("Query: users with walls => " + usersWalls);

		System.out.println("/***/");

		/*
		 * First query: use of ^links^ method|We can access the links obtained
		 * in the join result from the previous example using the ^links^
		 * method. Here we have Links array of size one. Every element of such
		 * an array is an object containing joins represented as mappings
		 * between ids of joined tables records. In this case, because the join
		 * is made only on two tables, users and wall, the array has only one
		 * object. In this example the variable links of type Links is used to
		 * keep the value of the first links object.|
		 */

		Links links = usersWalls.links()[0];
		debug("The user -> wall links are", links);

		System.out.println("/***/");

		/*
		 * First query: use of ^from^ and ^to^ methods|Because at this time our
		 * social network has only two users with walls, links object is
		 * consisted of two joins. Because the relation owns is oneToOne
		 * relation, every join is represented as first entity id as key (user
		 * id in this case) which is mapped to an array of second entity ids (in
		 * this case the array is of size one). In this code section it is shown
		 * how the first join results can be accessed by using ^from^ and ^to^
		 * method.|
		 */

		User exmpl_user = users.get(links.from(1));
		Wall exmpl_wall = walls.get(links.to(1)[0]);

		debug("First join ", exmpl_user + " to " + exmpl_wall);

		System.out.println("/***/");

		/*
		 * Left join|This is an example of how to make left join using
		 * ^leftJoin^ method. Here the result contains all user records.|
		 */

		JoinResult userWallsL = db.leftJoin(users.all(), owns, walls.all()).all();
		debug("Left join on users and walls result ", userWallsL);

		System.out.println("/***/");

		/*
		 * Right join|This is an example of how to make right join using
		 * ^rightJoin^ method. Here the result contains all wall records.|
		 */

		JoinResult userWallsR = db.rightJoin(users.all(), owns, walls.all()).all();
		debug("Right join on users and walls result", userWallsR);

		System.out.println("/***/");

		/*
		 * The use of ^exists^ method|This is an example of how to check if
		 * there exist any users who are friends with any other users. That can
		 * be done by using ^exists^ method.|
		 */

		boolean userFriends = db.join(users.all(), friend, users.all()).exists();
		debug("Right join on users and walls result", userFriends);

		System.out.println("/***/");

		/*
		 * More data|In order to proceed with our social network simulation,
		 * posts, comments and walls tables must be populated with some records.
		 * Also some relations need to be created with those records of the
		 * relations.|
		 */

		Post post1 = new Post("Yeee ... It started snowing ...");
		long pid1 = posts.insert(post1);

		Post post2 = new Post("Two be or not two be, that is the question :)");
		long pid2 = posts.insert(post2);
		Post post3 = new Post("Bazinga!!!");
		long pid3 = posts.insert(post3);

		Comment c1 = new Comment("Winter is coming to town :)");
		long cid1 = comments.insert(c1);

		Comment c2 = new Comment("Nice :)");
		long cid2 = comments.insert(c2);

		likedBy.link(pid1, id2);
		likedBy.link(pid1, id7);
		likedBy.link(pid1, id0);
		likedBy.link(pid2, id1);
		likedBy.link(pid3, id0);

		friend.link(id0, id2);
		friend.link(id1, id3);
		friend.link(id7, id0);
		friend.link(id0, id4);
		friend.link(id4, id2);

		writes.link(id1, cid1);
		writes.link(id2, cid2);

		belongsTo.link(c1, post1);
		belongsTo.link(c2, post1);

		likes.link(id4, pid3);
		likes.link(id7, pid1);
		likes.link(id4, pid1);

		likesComment.link(id0, cid1);

		madeBy.link(pid1, id2);

		System.out.println("/***/");

		/*
		 * Printing of relations|The relations also can be printed using ^print^
		 * method|
		 */

		debug("Relation: likes =>");
		likes.print();

		System.out.println("/***/");

		/*
		 * Relations: ^delink^ method| Let's say that user 4 no longer likes
		 * post 3, so he dislikes it. This is done by the ^delink^ method. It's
		 * purpose is to delete an existing relation between two entities.|
		 */

		debug("Relations \"likes\" before the dislike =>");
		likes.print();
		likes.delink(id4, pid3);
		debug("Relations \"likes\" after the dislike =>");
		likes.print();

		System.out.println("/***/");

		/*
		 * Example 1| Query 1. Friends of user X who liked post Y .|
		 */

		Ids<User> usrX = users.withIds(12);
		Ids<User> friends_1 = users.all();
		Ids<Post> posts_1 = posts.withIds(posts.ids()[0]);

		JoinResult jr1 = db.join(usrX, friend, friends_1).join(friends_1, likes, posts_1).all();

		debug(jr1);

		System.out.println("/***/");

		/*
		 * Example 2|Query 2. Friends of user A who liked posts posted by user
		 * B.|
		 */

		Ids<User> usrA = users.withIds(id0);
		Ids<User> usrB = users.withIds(id2);
		Ids<Post> posts_2 = posts.all();
		Ids<User> friends_2 = users.all();

		JoinResult jr2 = db.join(usrA, friend, friends_2).join(friends_2, likes, posts_2).join(posts_2, madeBy, usrB)
				.all();

		debug(jr2);

		System.out.println("/***/");

		/*
		 * Example 3|Query 3. Friends of user C who liked post D and are older
		 * than 25.|
		 */

		User usrs = users.queryHelper();
		users.createIndexOn(usrs.age);

		Ids<User> usrC = users.withIds(12);
		Search<User> older = users.where(usrs.age).gt(25);
		JoinResult jr3 = db.join(usrC, friend, older).join(older, likes, posts.all()).all();

		debug(jr3);

		System.out.println("/***/");

		/*
		 * Example 4|Query 4. All of user M friends who have written comment on
		 * post N.|
		 */

		Ids<User> usrM = users.withIds(12);
		Ids<User> friends_4 = users.all();
		Ids<Post> postN = posts.withIds(posts.ids()[0]);
		Ids<Comment> userComm = comments.all();

		JoinResult jr4 = db.join(usrM, friend, friends_4).join(friends_4, writes, userComm)
				.join(userComm, belongsTo, postN).all();
		debug(jr4);

		System.out.println("/***/");

		/*
		 * Example 5|Query 5. All mutual friends of users Z and T who have liked
		 * comment on post Q.|
		 */
		Ids<User> usrZ = users.withIds(id2);
		Ids<User> usrT = users.withIds(id4);
		Ids<User> friends_5 = users.all();
		Ids<Post> postQ = posts.withIds(posts.ids()[0]);
		Ids<Comment> usrComm = comments.all();

		JoinResult jr5 = db.join(usrZ, friend, friends_5).join(friends_5, friend, usrT)
				.join(friends_5, likesComment, userComm).join(userComm, belongsTo, postN).all();
		debug(jr5);

		System.out.println("/***/");

		/*
		 * Triggers|You can make your own trigger by implementing the Trigger
		 * interface on your trigger class. You have to implement the ^process^
		 * method. The triggers can be placed after or before some action is
		 * executed. The action could be insertion, deletion or updating of a
		 * table. The following code shows the use of ^after^, ^before^,
		 * ^deleted^, ^inserted^ ,^updated^ methods. The action of the trigger
		 * is assigned with ^run^ method.|
		 */

		db.after(User.class).deleted().run(new ExampleTrigger<User>());
		db.after(User.class).inserted().run(new ExampleTrigger<User>());
		db.after(User.class).updated().run(new ExampleTrigger<User>());

		db.before(User.class).deleted().run(new ExampleTrigger<User>());
		db.before(User.class).inserted().run(new ExampleTrigger<User>());
		db.before(User.class).updated().run(new ExampleTrigger<User>());

		System.out.println("/***/");

		/*
		 * Transaction|A transaction is started using ^startTransaction^ method.
		 * Then you write all the actions that you want to be executed in an
		 * atomic manner. Here the transaction consists of deleting users
		 * together with their walls. When one user is deleted, its wall has to
		 * be deleted too. If this actions are executed out of transaction, this
		 * scenario can happen: the user gets deleted, but something went wrong
		 * when the deletion of the wall should have taken place (for example
		 * the power went down after the user was deleted). That is why these
		 * actions should be put in a transaction. You end your transaction
		 * using the ^commit^ method.|
		 */

		Transaction tx = db.startTransaction();
		users.delete(user2.id);
		walls.delete(wall2.id);
		tx.commit();

		System.out.println("/***/");

		/*
		 * Transaction rollback|You can make a rollback of a transaction using
		 * ^rollback^ method. You desire to delete user 5 together with his
		 * wall. But user 5 has no wall. So the deletion of the unexisting wall
		 * will throw an exception, giving you the chance to make a rollback on
		 * the transaction and return to the previous state in the catch part.|
		 */

		Transaction tx2 = db.startTransaction();

		try {
			users.delete(id5);
			walls.delete(5);
			tx2.commit();
		} catch (Exception e) {
			tx2.rollback();
		}

		System.out.println("/***/");

		/*
		 * Deleting the content of a table|You can delete the content of a table
		 * using ^clear^ method.|
		 */

		users.clear();

		System.out.println("/***/");

		/*
		 * Shutdown the database|The database can be stopped gracefully with the
		 * ^shutdown^ method.|
		 */

		db.shutdown();

		/* END */
	}

	private static void debug(String caption, Object val) {
		if (val instanceof Object[]) {
			val = Arrays.toString((Object[]) val);
		} else if (val instanceof long[]) {
			val = Arrays.toString((long[]) val);
		}
		debug(caption + ": " + val);
	}

	private static void debug(Object val) {
		System.out.println(val);
	}

}
