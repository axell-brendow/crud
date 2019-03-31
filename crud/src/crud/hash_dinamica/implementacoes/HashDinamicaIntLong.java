package crud.hash_dinamica.implementacoes;

import java.util.ArrayList;

import crud.hash_dinamica.serializaveis.IntSerializavel;
import crud.hash_dinamica.serializaveis.LongSerializavel;
import crud.hash_dinamica.HashDinamica;

/**
 * Estrutura de hashing dinâmico para indexamento de registros.
 * 
 * @author Axell Brendow ( https://github.com/axell-brendow )
 */

public class HashDinamicaIntLong extends HashDinamica<IntSerializavel, LongSerializavel>
{
	public static final int PADRAO_NUMERO_DE_REGISTROS_POR_BUCKET = 21;
	
	/**
	 * Cria um objeto que gerencia uma hash dinâmica.
	 * 
	 * @param nomeDoArquivoDoDiretorio Nome do arquivo previamente usado para o diretório.
	 * @param nomeDoArquivoDosBuckets Nome do arquivo previamente usado para os buckets.
	 * Caso o arquivo não tenha sido criado ainda, ele será criado com este nome.
	 * @param numeroDeRegistrosPorBucket Numero de registros por bucket caso o arquivo
	 * não tenha sido criado ainda.
	 * @param funcaoHash Função de dispersão (hash) que será usada para as chaves. É
	 * importante ressaltar que essa função só precisa gerar valores dispersos, não
	 * importando o tamanho deles. Não utilize geração de números aleatórios.
	 * 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	
	public HashDinamicaIntLong(
		String nomeDoArquivoDoDiretorio,
		String nomeDoArquivoDosBuckets,
		int numeroDeRegistrosPorBucket
		/*Function<IntSerializavel, Integer> funcaoHash*/) throws NoSuchMethodException, SecurityException
	{
		super(
			nomeDoArquivoDoDiretorio,
			nomeDoArquivoDosBuckets,
			numeroDeRegistrosPorBucket,
			(short) Integer.BYTES,
			(short) Long.BYTES,
			IntSerializavel.class.getConstructor(),
			LongSerializavel.class.getConstructor(),
			(chave) -> { return chave.valor; }
		);
	}
	
	/**
	 * Cria um objeto que gerencia uma hash dinâmica.
	 * 
	 * @param nomeDoArquivoDoDiretorio Nome do arquivo previamente usado para o diretório.
	 * @param nomeDoArquivoDosBuckets Nome do arquivo previamente usado para os buckets.
	 * Caso o arquivo não tenha sido criado ainda, ele será criado com este nome.
	 * 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	
	public HashDinamicaIntLong(
		String nomeDoArquivoDoDiretorio,
		String nomeDoArquivoDosBuckets
		/*Function<IntSerializavel, Integer> funcaoHash*/) throws NoSuchMethodException, SecurityException
	{
		this(
			nomeDoArquivoDoDiretorio,
			nomeDoArquivoDosBuckets,
			PADRAO_NUMERO_DE_REGISTROS_POR_BUCKET
		);
	}
	
	/**
	 * Procura um registro na hash dinâmica com a chave informada.
	 * 
	 * @param chave Chave a ser procurada.
	 * 
	 * @return {@link java.lang.Long#MIN_VALUE} se o registro não
	 * for encontrado; o dado correspondente à chave caso contrário.
	 */
	
	public long pesquisarDadoPelaChave(int chave)
	{
		long dado = Long.MIN_VALUE;

		LongSerializavel longSerializavel = pesquisarDadoPelaChave(new IntSerializavel(chave));
		
		if (longSerializavel != null)
		{
			dado = longSerializavel.valor;
		}
		
		return dado;
	}
	
	/**
	 * Procura um registro na hash dinâmica com o dado informado.
	 * 
	 * @param dado Dado a ser procurado.
	 * 
	 * @return {@link java.lang.Integer#MIN_VALUE} se o registro não
	 * for encontrado; a chave correspondente ao dado caso contrário.
	 */
	
	public int pesquisarChavePeloDado(long dado)
	{
		int chave = Integer.MIN_VALUE;

		IntSerializavel intSerializavel = pesquisarChavePeloDado(new LongSerializavel(dado));
		
		if (intSerializavel != null)
		{
			chave = intSerializavel.valor;
		}
		
		return chave;
	}
	
	/**
	 * Exclui o registro no bucket com a chave e o dado informados.
	 * 
	 * @param chave Chave a ser excluída.
	 * @param dado Dado que corresponde à chave.
	 * 
	 * @return {@code true} se tudo der certo;
	 * {@code false} caso contrário.
	 */
	
	public boolean excluir(int chave, long dado)
	{
		return excluir(new IntSerializavel(chave), new LongSerializavel(dado));
	}
	
	/**
	 * Tenta excluir o primeiro registro com a chave informada.
	 * 
	 * @param chave Chave a ser excluída.
	 * 
	 * @return {@code true} se tudo der certo;
	 * {@code false} caso contrário.
	 */
	
	public boolean excluir(int chave)
	{
		return excluir(new IntSerializavel(chave));
	}
	
	/**
	 * Procura todos os registros com uma chave específica e gera
	 * uma lista com os dados correspondentes a essas chaves.
	 * 
	 * @param chave Chave a ser procurada.
	 * 
	 * @return lista com os dados correspondentes às chaves.
	 */
	
	public long[] listarDadosComAChave(int chave)
	{
		ArrayList<LongSerializavel> list = listarDadosComAChave(new IntSerializavel(chave));
		int listSize = list.size();
		
		long[] dados = new long[listSize];
		
		for (int i = 0; i < listSize; i++)
		{
			dados[i] = list.get(i).valor;
		}
		
		return dados;
	}
	
	/**
	 * Tenta inserir a chave e o dado na hash dinâmica.
	 * 
	 * @param chave Chave a ser inserida.
	 * @param dado Dado que corresponde à chave.
	 * 
	 * @return {@code true} se a chave e o dado forem inseridos.
	 * Caso contrário, {@code false}.
	 */
	
	public boolean inserir(int chave, long dado)
	{
		return inserir(new IntSerializavel(chave), new LongSerializavel(dado));
	}
}
