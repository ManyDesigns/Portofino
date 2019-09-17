DROP TABLE IF EXISTS public.supplier;
create table public.supplier (
    suppid int not null,
    name varchar(80) null,
    status varchar(2) not null,
    addr1 varchar(80) null,
    addr2 varchar(80) null,
    city varchar(80) null,
    state varchar(80) null,
    zip varchar(5) null,
    phone varchar(80) null,
    constraint pk_supplier primary key (suppid)
);

DROP TABLE IF EXISTS public.signon;
create table public.signon (
    username varchar(25) not null,
    password varchar(25)  not null,
    constraint pk_signon primary key (username)
);

DROP TABLE IF EXISTS public.account;
create table public.account (
    userid varchar(80) not null,
    email varchar(80) not null,
    firstname varchar(80) not null,
    lastname varchar(80) not null,
    status varchar(2)  null,
    addr1 varchar(80) not null,
    addr2 varchar(40) null,
    city varchar(80) not  null,
    state varchar(80) not null,
    zip varchar(20) not null,
    country varchar(20) not null,
    phone varchar(80) not null,
    constraint pk_account primary key (userid)
);

DROP TABLE IF EXISTS public.profile;
create table public.profile (
    userid varchar(80) not null,
    langpref varchar(80) not null,
    favcategory varchar(30),
    mylistopt boolean,
    banneropt boolean,
    constraint pk_profile primary key (userid)
);

DROP TABLE IF EXISTS public.bannerdata;
create table public.bannerdata (
    favcategory varchar(80) not null,
    bannername varchar(255)  null,
    constraint pk_bannerdata primary key (favcategory)
);

DROP TABLE IF EXISTS public.orders;
create table public.orders (
      orderid int not null,
      userid varchar(80) not null,
      orderdate date not null,
      shipaddr1 varchar(80) not null,
      shipaddr2 varchar(80) null,
      shipcity varchar(80) not null,
      shipstate varchar(80) not null,
      shipzip varchar(20) not null,
      shipcountry varchar(20) not null,
      billaddr1 varchar(80) not null,
      billaddr2 varchar(80)  null,
      billcity varchar(80) not null,
      billstate varchar(80) not null,
      billzip varchar(20) not null,
      billcountry varchar(20) not null,
      courier varchar(80) not null,
      totalprice decimal(10,2) not null,
      billtofirstname varchar(80) not null,
      billtolastname varchar(80) not null,
      shiptofirstname varchar(80) not null,
      shiptolastname varchar(80) not null,
      creditcard varchar(80) not null,
      exprdate varchar(7) not null,
      cardtype varchar(80) not null,
      locale varchar(80) not null,
      constraint pk_orders primary key (orderid)
);

DROP TABLE IF EXISTS public.orderstatus;
create table public.orderstatus (
      orderid int not null,
      linenum int not null,
      timestamp date not null,
      status varchar(2) not null,
      constraint pk_orderstatus primary key (orderid, linenum)
);

DROP TABLE IF EXISTS public.lineitem;
create table public.lineitem (
      orderid int not null,
      linenum int not null,
      itemid varchar(10) not null,
      quantity int not null,
      unitprice decimal(10,2) not null,
      constraint pk_lineitem primary key (orderid, linenum)
);

DROP TABLE IF EXISTS public.category;
create table public.category (
	catid varchar(10) not null,
	name varchar(80) null,
	descn varchar(255) null,
	constraint pk_category primary key (catid)
);

DROP TABLE IF EXISTS public.product;
create table public.product (
    productid varchar(10) not null,
    category varchar(10) not null,
    name varchar(80) null,
    descn varchar(255) null,
    constraint pk_product primary key (productid),
        constraint fk_product_1 foreign key (category)
        references category (catid)
);

create index productCat on public.product (category);
create index productName on public.product (name);

DROP TABLE IF EXISTS public.item;
create table public.item (
    itemid varchar(10) not null,
    productid varchar(10) not null,
    listprice decimal(10,2) null,
    unitcost decimal(10,2) null,
    supplier int null,
    status varchar(2) null,
    attr1 varchar(80) null,
    attr2 varchar(80) null,
    attr3 varchar(80) null,
    attr4 varchar(80) null,
    attr5 varchar(80) null,
    constraint pk_item primary key (itemid),
        constraint fk_item_1 foreign key (productid)
        references product (productid),
        constraint fk_item_2 foreign key (supplier)
        references supplier(suppid)
);

create index itemProd on public.item (productid);

DROP TABLE IF EXISTS public.inventory;
create table public.inventory (
    itemid varchar(10) not null,
    qty int not null,
    constraint pk_inventory primary key (itemid)
);

DROP TABLE IF EXISTS sequence;
CREATE TABLE sequence
(
    name               varchar(30)  not null,
    nextid            int          not null,
    constraint pk_sequence primary key (name)
);
