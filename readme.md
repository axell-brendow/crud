# CRUD
**Reposit�rio do CRUD de AEDs III**

O projeto traz a implementa��o de um sistema de gerenciamento para supermercados. Inclui produtos, categorias, compras, clientes, funcion�rios e um sistema de autentica��o.

O projeto utiliza algumas estruturas de dados para indexamento de registros e � capaz de criar �ndices/conex�es entre diferentes tipos de entidades.

Al�m disso, o gerenciamento da base de dados foi implementado incluindo algoritmos de criptografia e compacta��o que ajudam na seguran�a e manuten��o dos dados.

O arquivo .jar disponibilizado no reposit�rio � funcional e pode ser testado. Cabe um alerta: a base de dados � exclu�da a cada execu��o.

Relacionamento entre as entidades no banco de dados:

CATEGORIA       ->	PRODUTO         (1:N)

PRODUTO         ->	CATEGORIA       (1:1)

PRODUTO         ->	ITEM_COMPRADO   (1:N)

ITEM_COMPRADO   ->	PRODUTO         (1:1)

COMPRA          ->	ITEM_COMPRADO   (1:N)

ITEM_COMPRADO   ->	COMPRA          (1:1)

CLIENTE         ->	COMPRA          (1:N)

COMPRA          ->	CLIENTE         (1:1)
