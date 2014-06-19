create table users(
	id			int 			not null primary key	IDENTITY(1,1),
	username	varchar(20)		not null,
	password	varchar(20)		not null,
	realname	varchar(20)		null,
	status		varchar(20)		null default 'enabled',
	role		int				null default 0,
	regtime		datetime		null
)

