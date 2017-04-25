pedal-dialect
=============

Pedal-dialect, a member of the Pedal family ([pedal-tx](https://github.com/eclecticlogic/pedal-tx), [pedal-loader](https://github.com/eclecticlogic/pedal-loader)), is a collection of dialect (database) and provider (e.g. Hibernate) specific implementations for use with JPA. It enables non-standard SQL data types (arrays, sets, bit-sets) and dialect specific features such as support for the Postgresql Copy command.

## Feature Highlights

- Provider support (for Hibernate)
	- Access current schema name
	- Get table name for given entity (including when remapped by orm.xml)
	- Hibernate user-type (mutable and non-mutable) base classes.
	- List and Set user-types.
- Postgresql specific features
	- Adaptation of List/Set type for Hibernate
	- Hibernate user-type for Postgres bit arra
	- Support for using the COPY command directly with JPA entity classes.

## Getting started

Download the pedal-dialect jar from Maven central:

```
	<groupId>com.eclecticlogic</groupId>
	<artifactId>pedal-dialect</artifactId>
	<version>1.1.0</version>
```

Minimum dependencies that you need to provide in your application:

1. slf4j (over logback or log4j) v1.7.7 or higher
2. Spring-boot jpa 1.2 or
2. spring-tx, spring-context and spring-orm v4.0 or higher
4. hibernate-core and hibernate-entitymanager 4.3 or higher.
5. JDBC4 compliant driver and connection pool manager (BoneCP, HikariCP, Apache Commons DBCP2 and Tomcat JDBC are supported).


## Configuration

To enable the `ProviderAccess` interface, create an instance of `HibernateProviderAccessSpiImpl` and give it a reference to the `EntityManagerFactory`. Here is the java code to use in a Spring configuration class:

    
    @Bean
    HibernateProviderAccessSpiImpl hibernateProvider(EntityManagerFactory factory) {
        HibernateProviderAccessSpiImpl impl = new HibernateProviderAccessSpiImpl();
        impl.setEntityManagerFactory(factory);
        return impl;
    }


To enable the `CopyCommand` create an instance of it and set the ProviderAccess and an instance of a `ConnectionAccessor`. For e.g., to use it with Tomcat, the following Bean creator method can be used:

    

    @Bean
    public CopyCommand copyCommand(ProviderAccessSpi provider) {
        CopyCommand command = new CopyCommand();
        command.setProviderAccessSpi(provider);
        command.setConnectionAccessor(new TomcatJdbcConnectionAccessor());
        return command;
    }

## Posgresql User Types

Pedal provides support (via Hibernate user-types) for a number of non-standard Postgresql data types that can really help streamline your data model. Among these are array types and bits.

Pedal supports `List` and `Set` types for `String`, `Integer`, `Long`, `Date`, `Boolean` and `BigDecimal` (i.e., collections of standard sql-types). To use `List` and `Set` data types, simply annotate your JPA/Hibernate entity as shown.

```

    @Column(name = "authorizations", nullable = false)
    @Type(type = "com.eclecticlogic.pedal.provider.hibernate.SetType", parameters = @Parameter(name = ArrayType.DIALECT_PRIMITIVE_NAME, value = PostgresqlArrayPrimitiveName.STRING))
    public Set<String> getAuthorizations() {
        return this.authorizations;
    }


    @Column(name = "scores")
    @Type(type = "com.eclecticlogic.pedal.provider.hibernate.ListType", parameters = { @Parameter(name = ArrayType.DIALECT_PRIMITIVE_NAME, value = PostgresqlArrayPrimitiveName.LONG) })
    public List<Long> getScores() {
        return this.scores;
    }

```

Pedal allows you to control how empty collections are treated - by default they are written as null values in the database, but you can change that:

```

    @Column(name = "gpa")
    @Type(type = "com.eclecticlogic.pedal.provider.hibernate.ListType", parameters = {
            @Parameter(name = ArrayType.DIALECT_PRIMITIVE_NAME, value = PostgresqlArrayPrimitiveName.LONG),
            @Parameter(name = ArrayType.EMPTY_IS_NULL, value = "false") })
    public List<Long> getGpa() {
        return gpa;
    }


```

Pedal also allow mapping of Postgresql bit array to `java.util.BitSet`. 

```

    @Column(name = "countries", nullable = false, length = 7)
    @Type(type = "com.eclecticlogic.pedal.provider.hibernate.dialect.PostgresqlBitStringUserType", parameters = @Parameter(name = PostgresqlBitStringUserType.BIT_LENGTH, value = "7"))
    public BitSet getCountries() {
        return this.countries;
    }


```

Note: The BIT_LENGTH parameter is required because Hibernate User-Types cannot access the JPA annotation.

## Copy Command features

The Postgresql [Copy](http://www.postgresql.org/docs/9.1/static/sql-copy.html) command provides a very high performance insert capability. One can easily achieve 100k inserts per second using it. For background and features that are bypassed by the copy command refer to the official [documentation](http://www.postgresql.org/docs/9.1/static/sql-copy.html).

The copy command works in text or binary mode. Our tests show that the binary mode produces a larger data stream and therefore actually results in slower performance over the network. The text mode requires data to be encoded into delimited columns and rows. Such a format has a high-impedance mismatch with the typical manner in which objects are handled in an ORM. The Copy command feature of pedal overcomes this mismatch by allowing a collection of JPA entities written to the database. The pedal framework does the work of assembling the text encoding of the data in the JPA entities and it does so by creating a Javassist based custom class to optimize on performance.

The `CopyCommand` implementation in pedal currently has the following restrictions/limitations:

1. `@Column` annotation must be present and is only supported on getter methods.
2. `@Column` annotation must have column name in it or there should be an `@AttributeOverrides` or `@AttributeOverride` class-level annotation with the column name.
3. `@Convert` annotation is only support when applied to the getter.
4. Array types can only be arrays of primitives. Postgresql `bit` arrays are supported if the entity field data type is `java.util.BitSet`. Apply the `@CopyAsBitString` annotation to the getter to support writing to the Postgresql bit array. The `@Column` annotation must have the length set to the bit array length in Postgresql.
5. Embedded id support if `@EmbeddedId` annotation is present and `@AttributeOverrides` annotation denotes pk columns. See `Planet` class in the test.
6. No specific distinction between Temporal `TIMESTAMP` and `DATE`.

The `CopyCommand` does support a generic mechanism to support custom-types or work-around the above limitations using a `ConversionHelper`. For any field where you want to define custom conversion, apply the `@CopyConverter` annotation with a reference to an implementation of a suitable `ConversionHelper`.

Here is the general pattern of usage for the Copy Command:


        @PersistenceContext
        private EntityManager entityManager;

        ...        

        
        CopyList<ExoticTypes> list = new CopyList<>();

        // The copy-command can insert 100k of these per second.
        for (int i = 0; i < 10; i++) {
            MyEntity entity = new MyEntity();
            entity.setSomeField("value");
            entity.setSomeOtherField("value 2");
            list.add(et);
        }

        copyCommand.insert(entityManager, list);


## Release Notes

### 1.3.3

- Refactored copy command and added supported for ignoring non-insertable columns in CopyCommand.

### 1.2.1

- Exclude getter methods that take parameters from being considered for copy-command field list.

### 1.2.0 

- Upgraded to support Hibernate 5.2.0. There is a breaking change in the UserType interface.

### 1.1.1

- Fixed incorrect copy-command value when non-null custom value converted usign @Convert annotation results in null value.

### 1.1.0

- CopyCommand's use of Javassist is now tolerant of Spring's "compound jar" or bootable jar format.
- CopyCommand now supports property of type that is annotated with `@Embeddable`. The embedded type with one or more columns should have all db-mapped fields annotated with `@Column`. One can override the column names with the @AttributeOverrides annotation against the getter of the embedded type. However, if using @AttributeOverrides, every column should be overridden, even if the name remains the same.

### 1.0.3

- `PostgresqlBitStringUserType` converts to BitSet instead of List<Boolean>.

### 1.0.2

- ConversionHelper has been generified. See [`ExoticTypes`](https://raw.githubusercontent.com/eclecticlogic/pedal-dialect/master/src/test/java/com/eclecticlogic/pedal/dm/ExoticTypes.java) class in src/test/java of the source distribution for an example.

### 1.0.1

- Added UUIDBasedIdGenerator, a Hibernate id generator based on UUIDs. 


