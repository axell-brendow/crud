package crud;

import java.io.*;

// �rvore B+ para ser usada como �ndice direto de algum arquivo de entidades
// CHAVE: Int   (usado para id de alguma entidade)
// VALOR: Long  (usado para endere�o do registro dessa entidade no arquivo)

public class Indice {

    private int  ordem;                 // N�mero m�ximo de filhos que uma p�gina pode conter
    private int  maxElementos;          // Vari�vel igual a ordem - 1 para facilitar a clareza do c�digo
    private int  maxFilhos;             // Vari�vel igual a ordem para facilitar a clareza do c�digo
    private RandomAccessFile arquivo;   // Arquivo em que a �rvore ser� armazenada
    private String nomeArquivo;
    
    // Vari�veis usadas nas fun��es recursivas (j� que n�o � poss�vel passar valores por refer�ncia)
    private int  chaveAux;
    private long  dadoAux;
    private long paginaAux;
    private boolean cresceu;
    private boolean diminuiu;
    
    // Esta classe representa uma p�gina da �rvore (folha ou n�o folha). 
    private class Pagina {

        protected int    ordem;                 // N�mero m�ximo de filhos que uma p�gina pode ter
        protected int    maxElementos;          // Vari�vel igual a ordem - 1 para facilitar a clareza do c�digo
        protected int    maxFilhos;             // Vari�vel igual a ordem  para facilitar a clareza do c�digo
        protected int    n;                     // N�mero de elementos presentes na p�gina
        protected int[]  chaves;                // Chaves
        protected long[] dados;                 // Dados associados �s chaves
        protected long   proxima;               // Pr�xima folha, quando a p�gina for uma folha
        protected long[] filhos;                // Vetor de ponteiros para os filhos
        protected int    TAMANHO_ELEMENTO;      // Os elementos s�o de tamanho fixo
        protected int    TAMANHO_PAGINA;        // A p�gina ser� de tamanho fixo, calculado a partir da ordem

        // Construtor da p�gina
        public Pagina(int o) {

            // Inicializa��o dos atributos
            n = 0;
            ordem = o;
            maxFilhos = o;
            maxElementos = o-1;
            chaves = new int[maxElementos];
            dados  = new long[maxElementos];
            filhos = new long[maxFilhos];
            proxima = -1;
            
            // Cria��o de uma p�gina v�zia
            for(int i=0; i<maxElementos; i++) {  
                chaves[i] = 0;
                dados[i]  = -1;
                filhos[i] = -1;
            }
            filhos[maxFilhos-1] = -1;
            
            // C�lculo do tamanho (fixo) da p�gina
            // n -> 4 bytes
            // cada elemento -> 12 bytes (int + long)
            // cada ponteiro de filho -> 8 bytes (long)
            // �ltimo filho -> 8 bytes (long)
            // ponteiro pr�ximo -> 8 bytes
            TAMANHO_ELEMENTO = 12;
            TAMANHO_PAGINA = 4 + maxElementos*TAMANHO_ELEMENTO + maxFilhos*8 + 16;
        }
        
        // Retorna o vetor de bytes que representa a p�gina para armazenamento em arquivo
        protected byte[] getBytes() throws IOException {
            
            // Um fluxo de bytes � usado para constru��o do vetor de bytes
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            
            // Quantidade de elementos presentes na p�gina
            out.writeInt(n);
            
            // Escreve todos os elementos
            int i=0;
            while(i<n) {
                out.writeLong(filhos[i]);
                out.writeInt(chaves[i]);
                out.writeLong(dados[i]);
                i++;
            }
            out.writeLong(filhos[i]);
            
            // Completa o restante da p�gina com registros vazios
            byte[] elementoVazio = new byte[TAMANHO_ELEMENTO];
            while(i<maxElementos){
                out.write(elementoVazio);
                out.writeLong(filhos[i+1]);
                i++;
            }

            // Escreve o ponteiro para a pr�xima p�gina
            out.writeLong(proxima);
            
            // Retorna o vetor de bytes que representa a p�gina
            return ba.toByteArray();
        }

        
        // Reconstr�i uma p�gina a partir de um vetor de bytes lido no arquivo
        protected void setBytes(byte[] buffer) throws IOException {
            
            // Usa um fluxo de bytes para leitura dos atributos
            ByteArrayInputStream ba = new ByteArrayInputStream(buffer);
            DataInputStream in = new DataInputStream(ba);
            
            // L� a quantidade de elementos da p�gina
            n = in.readInt();
            
            // L� todos os elementos (reais ou vazios)
            int i=0;
            while(i<maxElementos) {
                filhos[i]  = in.readLong();
                chaves[i] = in.readInt();
                dados[i]   = in.readLong(); 
                i++;
            }
            filhos[i] = in.readLong();
            proxima = in.readLong();
        }
    }
    
    // ------------------------------------------------------------------------------
        
    
    public Indice(int o, String na) throws IOException {
        
        // Inicializa os atributos da �rvore
        ordem = o;
        maxElementos = o-1;
        maxFilhos = o;
        nomeArquivo = na;
        
        // Abre (ou cria) o arquivo, escrevendo uma raiz vazia, se necess�rio.
        arquivo = new RandomAccessFile(nomeArquivo,"rw");
        if(arquivo.length()<8) 
            arquivo.writeLong(-1);  // raiz vazia
    }
    
    // Testa se a �rvore est� vazia. Uma �rvore vazia � identificada pela raiz == -1
    public boolean vazia() throws IOException {
        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();
        return raiz == -1;
    }
    
        
    // Busca recursiva por um elemento a partir da chave. Este metodo invoca 
    // o m�todo recursivo buscar1, passando a raiz como refer�ncia.
    public long buscar(int c) throws IOException {
        
        // Recupera a raiz da �rvore
        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();
        
        // Executa a busca recursiva
        if(raiz!=-1)
            return buscar1(c,raiz);
        else
            return -1;
    }
    
    // Busca recursiva. Este m�todo recebe a refer�ncia de uma p�gina e busca
    // pela chave na mesma. A busca continua pelos filhos, se houverem.
    private long buscar1(int chave, long pagina) throws IOException {
        
        // Como a busca � recursiva, a descida para um filho inexistente
        // (filho de uma p�gina folha) retorna um valor negativo.
        if(pagina==-1)
            return -1;
        
        // Reconstr�i a p�gina passada como refer�ncia a partir 
        // do registro lido no arquivo
        arquivo.seek(pagina);
        Pagina pa = new Pagina(ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.setBytes(buffer);
 
        // Encontra o ponto em que a chave deve estar na p�gina
        // Primeiro passo - todas as chaves menores que a chave buscada s�o ignoradas
        int i=0;
        while(i<pa.n && chave>pa.chaves[i]) {
            i++;
        }
        
        // Chave encontrada (ou pelo menos o ponto onde ela deveria estar).
        // Segundo passo - testa se a chave � a chave buscada e se est� em uma folha
        // Obs.: em uma �rvore B+, todas as chaves v�lidas est�o nas folhas
        if(i<pa.n && pa.filhos[0]==-1 && chave==pa.chaves[i]) {
            return pa.dados[i];
        }
        
        // Terceiro passo - ainda n�o � uma folha, continua a busca recursiva pela �rvore
        if(i==pa.n || chave<pa.chaves[i])
            return buscar1(chave, pa.filhos[i]);
        else
            return buscar1(chave, pa.filhos[i+1]);
    }
        
    // Atualiza recursivamente um valor a partir da sua chave. Este metodo invoca 
    // o m�todo recursivo atualizar1, passando a raiz como refer�ncia.
    public boolean atualizar(int c, long d) throws IOException {
        
        // Recupera a raiz da �rvore
        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();
        
        // Executa a busca recursiva
        if(raiz!=-1)
            return atualizar1(c,d,raiz);
        else
            return false;
    }
    
    // Atualiza��o recursiva. Este m�todo recebe a refer�ncia de uma p�gina, uma
    // chave de busca e o dado correspondente a ela. 
    private boolean atualizar1(int chave, long dado, long pagina) throws IOException {
        
        // Como a busca � recursiva, a descida para um filho inexistente
        // (filho de uma p�gina folha) retorna um valor negativo.
        if(pagina==-1)
            return false;
        
        // Reconstr�i a p�gina passada como refer�ncia a partir 
        // do registro lido no arquivo
        arquivo.seek(pagina);
        Pagina pa = new Pagina(ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.setBytes(buffer);
 
        // Encontra o ponto em que a chave deve estar na p�gina
        // Primeiro passo - todas as chaves menores que a chave buscada s�o ignoradas
        int i=0;
        while(i<pa.n && chave>pa.chaves[i]) {
            i++;
        }
        
        // Chave encontrada (ou pelo menos o ponto onde ela deveria estar).
        // Segundo passo - testa se a chave � a chave buscada e se est� em uma folha
        // Obs.: em uma �rvore B+, todas as chaves v�lidas est�o nas folhas
        if(i<pa.n && pa.filhos[0]==-1 && chave==pa.chaves[i]) {
            pa.dados[i] = dado;
            arquivo.seek(pagina);
            arquivo.write(pa.getBytes());
            return true;
        }
        
        // Terceiro passo - ainda n�o � uma folha, continua a busca recursiva pela �rvore
        if(i==pa.n || chave<pa.chaves[i])
            return atualizar1(chave, dado, pa.filhos[i]);
        else
            return atualizar1(chave, dado, pa.filhos[i+1]);
    }
        
    
    // Inclus�o de novos elementos na �rvore. A inclus�o � recursiva. A primeira
    // fun��o chama a segunda recursivamente, passando a raiz como refer�ncia.
    // Eventualmente, a �rvore pode crescer para cima.
    public boolean inserir(int c, long d) throws IOException {

        // Valida��o da chave
        if(c<0) {
            System.out.println( "Chave n�o pode ser negativa" );
            return false;
        }
            
        // Carrega a raiz
        arquivo.seek(0);       
        long pagina;
        pagina = arquivo.readLong();

        // O processo de inclus�o permite que os valores passados como refer�ncia
        // sejam substitu�dos por outros valores, para permitir a divis�o de p�ginas
        // e crescimento da �rvore. Assim, s�o usados os valores globais chaveAux 
        // e dadoAux. Quando h� uma divis�o, a chave e o valor promovidos s�o armazenados
        // nessas vari�veis.
        chaveAux = c;
        dadoAux = d;
        
        // Se houver crescimento, ent�o ser� criada uma p�gina extra e ser� mantido um
        // ponteiro para essa p�gina. Os valores tamb�m s�o globais.
        paginaAux = -1;
        cresceu = false;
                
        // Chamada recursiva para a inser��o da chave e do valor
        // A chave e o valor n�o s�o passados como par�metros, porque s�o globais
        boolean inserido = inserir1(pagina);
        
        // Testa a necessidade de cria��o de uma nova raiz.
        if(cresceu) {
            
            // Cria a nova p�gina que ser� a raiz. O ponteiro esquerdo da raiz
            // ser� a raiz antiga e o seu ponteiro direito ser� para a nova p�gina.
            Pagina novaPagina = new Pagina(ordem);
            novaPagina.n = 1;
            novaPagina.chaves[0] = chaveAux;
            novaPagina.dados[0]  = dadoAux;
            novaPagina.filhos[0] = pagina;
            novaPagina.filhos[1] = paginaAux;
            
            // Acha o espa�o em disco. Nesta vers�o, todas as novas p�ginas
            // s�o escrita no fim do arquivo.
            arquivo.seek(arquivo.length());
            long raiz = arquivo.getFilePointer();
            arquivo.write(novaPagina.getBytes());
            arquivo.seek(0);
            arquivo.writeLong(raiz);
        }
        
        return inserido;
    }
    
    
    // Fun��o recursiva de inclus�o. A fun��o passa uma p�gina de refer�ncia.
    // As inclus�es s�o sempre feitas em uma folha.
    private boolean inserir1(long pagina) throws IOException {
        
        // Testa se passou para o filho de uma p�gina folha. Nesse caso, 
        // inicializa as vari�veis globais de controle.
        if(pagina==-1) {
            cresceu = true;
            paginaAux = -1;
            return false;
        }
        
        // L� a p�gina passada como refer�ncia
        arquivo.seek(pagina);
        Pagina pa = new Pagina(ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.setBytes(buffer);
        
        // Busca o pr�ximo ponteiro de descida. Como pode haver repeti��o
        // da primeira chave, a segunda tamb�m � usada como refer�ncia.
        // Nesse primeiro passo, todos os pares menores s�o ultrapassados.
        int i=0;
        while(i<pa.n && chaveAux>pa.chaves[i]) {
            i++;
        }
        
        // Testa se a chave j� existe em uma folha. Se isso acontecer, ent�o 
        // a inclus�o � cancelada.
        if(i<pa.n && pa.filhos[0]==-1 && chaveAux==pa.chaves[i]) {
            cresceu = false;
            return false;
        }
        
        // Continua a busca recursiva por uma nova p�gina. A busca continuar� at� o
        // filho inexistente de uma p�gina folha ser alcan�ado.
        boolean inserido;
        if(i==pa.n || chaveAux<pa.chaves[i])
            inserido = inserir1(pa.filhos[i]);
        else
            inserido = inserir1(pa.filhos[i+1]);
        
        // A partir deste ponto, as chamadas recursivas j� foram encerradas. 
        // Assim, o pr�ximo c�digo s� � executado ao retornar das chamadas recursivas.

        // A inclus�o j� foi resolvida por meio de uma das chamadas recursivas. Nesse
        // caso, apenas retorna para encerrar a recurs�o.
        // A inclus�o pode ter sido resolvida porque a chave j� existia (inclus�o inv�lida)
        // ou porque o novo elemento coube em uma p�gina existente.
        if(!cresceu)
            return inserido;
        
        // Se tiver espa�o na p�gina, faz a inclus�o nela mesmo
        if(pa.n<maxElementos) {

            // Puxa todos elementos para a direita, come�ando do �ltimo
            // para gerar o espa�o para o novo elemento
            for(int j=pa.n; j>i; j--) {
                pa.chaves[j] = pa.chaves[j-1];
                pa.dados[j] = pa.dados[j-1];
                pa.filhos[j+1] = pa.filhos[j];
            }
            
            // Insere o novo elemento
            pa.chaves[i] = chaveAux;
            pa.dados[i] = dadoAux;
            pa.filhos[i+1] = paginaAux;
            pa.n++;
            
            // Escreve a p�gina atualizada no arquivo
            arquivo.seek(pagina);
            arquivo.write(pa.getBytes());
            
            // Encerra o processo de crescimento e retorna
            cresceu=false;
            return true;
        }
        
        // O elemento n�o cabe na p�gina. A p�gina deve ser dividida e o elemento
        // do meio deve ser promovido (sem retirar a refer�ncia da folha).
        
        // Cria uma nova p�gina
        Pagina np = new Pagina(ordem);
        
        // Copia a metade superior dos elementos para a nova p�gina,
        // considerando que maxElementos pode ser �mpar
        int meio = maxElementos/2;
        for(int j=0; j<(maxElementos-meio); j++) {    
            
            // copia o elemento
            np.chaves[j] = pa.chaves[j+meio];
            np.dados[j] = pa.dados[j+meio];   
            np.filhos[j+1] = pa.filhos[j+meio+1];  
            
            // limpa o espa�o liberado
            pa.chaves[j+meio] = 0;
            pa.dados[j+meio] = 0;
            pa.filhos[j+meio+1] = -1;
        }
        np.filhos[0] = pa.filhos[meio];
        np.n = maxElementos-meio;
        pa.n = meio;
        
        // Testa o lado de inser��o
        // Caso 1 - Novo registro deve ficar na p�gina da esquerda
        if(i<=meio) {   
            
            // Puxa todos os elementos para a direita
            for(int j=meio; j>0 && j>i; j--) {
                pa.chaves[j] = pa.chaves[j-1];
                pa.dados[j] = pa.dados[j-1];
                pa.filhos[j+1] = pa.filhos[j];
            }
            
            // Insere o novo elemento
            pa.chaves[i] = chaveAux;
            pa.dados[i] = dadoAux;
            pa.filhos[i+1] = paginaAux;
            pa.n++;
            
            // Se a p�gina for folha, seleciona o primeiro elemento da p�gina 
            // da direita para ser promovido, mantendo-o na folha
            if(pa.filhos[0]==-1) {
                chaveAux = np.chaves[0];
                dadoAux = np.dados[0];
            }
            
            // caso contr�rio, promove o maior elemento da p�gina esquerda
            // removendo-o da p�gina
            else {
                chaveAux = pa.chaves[pa.n-1];
                dadoAux = pa.dados[pa.n-1];
                pa.chaves[pa.n-1] = 0;
                pa.dados[pa.n-1] = 0;
                pa.filhos[pa.n] = -1;
                pa.n--;
            }
        } 
        
        // Caso 2 - Novo registro deve ficar na p�gina da direita
        else {
            int j;
            for(j=maxElementos-meio; j>0 && chaveAux<np.chaves[j-1]; j--) {
                np.chaves[j] = np.chaves[j-1];
                np.dados[j] = np.dados[j-1];
                np.filhos[j+1] = np.filhos[j];
            }
            np.chaves[j] = chaveAux;
            np.dados[j] = dadoAux;
            np.filhos[j+1] = paginaAux;
            np.n++;

            // Seleciona o primeiro elemento da p�gina da direita para ser promovido
            chaveAux = np.chaves[0];
            dadoAux = np.dados[0];
            
            // Se n�o for folha, remove o elemento promovido da p�gina
            if(pa.filhos[0]!=-1) {
                for(j=0; j<np.n-1; j++) {
                    np.chaves[j] = np.chaves[j+1];
                    np.dados[j] = np.dados[j+1];
                    np.filhos[j] = np.filhos[j+1];
                }
                np.filhos[j] = np.filhos[j+1];
                
                // apaga o �ltimo elemento
                np.chaves[j] = 0;
                np.dados[j] = 0;
                np.filhos[j+1] = -1;
                np.n--;
            }

        }
        
        // Se a p�gina era uma folha e apontava para outra folha, 
        // ent�o atualiza os ponteiros dessa p�gina e da p�gina nova
        if(pa.filhos[0]==-1) {
            np.proxima=pa.proxima;
            pa.proxima = arquivo.length();
        }

        // Grava as p�ginas no arquivos arquivo
        paginaAux = arquivo.length();
        arquivo.seek(paginaAux);
        arquivo.write(np.getBytes());

        arquivo.seek(pagina);
        arquivo.write(pa.getBytes());
        
        return true;
    }

    
    // Remo��o elementos na �rvore. A remo��o � recursiva. A primeira
    // fun��o chama a segunda recursivamente, passando a raiz como refer�ncia.
    // Eventualmente, a �rvore pode reduzir seu tamanho, por meio da exclus�o da raiz.
    public boolean excluir(int chave) throws IOException {
                
        // Encontra a raiz da �rvore
        arquivo.seek(0);       
        long pagina;                
        pagina = arquivo.readLong();

        // vari�vel global de controle da redu��o do tamanho da �rvore
        diminuiu = false;  
                
        // Chama recursivamente a exclus�o de registro (na chave1Aux e no 
        // chave2Aux) passando uma p�gina como refer�ncia
        boolean excluido = excluir1(chave, pagina);
        
        // Se a exclus�o tiver sido poss�vel e a p�gina tiver reduzido seu tamanho,
        // por meio da fus�o das duas p�ginas filhas da raiz, elimina essa raiz
        if(excluido && diminuiu) {
            
            // L� a raiz
            arquivo.seek(pagina);
            Pagina pa = new Pagina(ordem);
            byte[] buffer = new byte[pa.TAMANHO_PAGINA];
            arquivo.read(buffer);
            pa.setBytes(buffer);
            
            // Se a p�gina tiver 0 elementos, apenas atualiza o ponteiro para a raiz,
            // no cabe�alho do arquivo, para o seu primeiro filho.
            if(pa.n == 0) {
                arquivo.seek(0);
                arquivo.writeLong(pa.filhos[0]);  
            }
        }
         
        return excluido;
    }
    

    // Fun��o recursiva de exclus�o. A fun��o passa uma p�gina de refer�ncia.
    // As exclus�es s�o sempre feitas em folhas e a fus�o � propagada para cima.
    private boolean excluir1(int chave, long pagina) throws IOException {
        
        // Declara��o de vari�veis
        boolean excluido=false;
        int diminuido;
        
        // Testa se o registro n�o foi encontrado na �rvore, ao alcan�ar uma folha
        // inexistente (filho de uma folha real)
        if(pagina==-1) {
            diminuiu=false;
            return false;
        }
        
        // L� o registro da p�gina no arquivo
        arquivo.seek(pagina);
        Pagina pa = new Pagina(ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.setBytes(buffer);

        // Encontra a p�gina em que a chave est� presente
        // Nesse primeiro passo, salta todas as chaves menores
        int i=0;
        while(i<pa.n && chave>pa.chaves[i]) {
            i++;
        }

        // Chaves encontradas em uma folha
        if(i<pa.n && pa.filhos[0]==-1 && chave==pa.chaves[i]) {

            // Puxa todas os elementos seguintes para uma posi��o anterior, sobrescrevendo
            // o elemento a ser exclu�do
            int j;
            for(j=i; j<pa.n-1; j++) {
                pa.chaves[j] = pa.chaves[j+1];
                pa.dados[j] = pa.dados[j+1];
            }
            pa.n--;
            
            // limpa o �ltimo elemento
            pa.chaves[pa.n] = 0;
            pa.dados[pa.n] = 0;
            
            // Atualiza o registro da p�gina no arquivo
            arquivo.seek(pagina);
            arquivo.write(pa.getBytes());
            
            // Se a p�gina contiver menos elementos do que o m�nimo necess�rio,
            // indica a necessidade de fus�o de p�ginas
            diminuiu = pa.n<maxElementos/2;
            return true;
        }

        // Se a chave n�o tiver sido encontrada (observar o return true logo acima),
        // continua a busca recursiva por uma nova p�gina. A busca continuar� at� o
        // filho inexistente de uma p�gina folha ser alcan�ado.
        // A vari�vel diminu�do mantem um registro de qual p�gina eventualmente 
        // pode ter ficado com menos elementos do que o m�nimo necess�rio.
        // Essa p�gina ser� filha da p�gina atual
        if(i==pa.n || chave<pa.chaves[i]) {
            excluido = excluir1(chave, pa.filhos[i]);
            diminuido = i;
        } else {
            excluido = excluir1(chave, pa.filhos[i+1]);
            diminuido = i+1;
        }
        
        
        // A partir deste ponto, o c�digo � executado ap�s o retorno das chamadas
        // recursivas do m�todo
        
        // Testa se h� necessidade de fus�o de p�ginas
        if(diminuiu) {

            // Carrega a p�gina filho que ficou com menos elementos do 
            // do que o m�nimo necess�rio
            long paginaFilho = pa.filhos[diminuido];
            Pagina pFilho = new Pagina(ordem);
            arquivo.seek(paginaFilho);
            arquivo.read(buffer);
            pFilho.setBytes(buffer);
            
            // Cria uma p�gina para o irm�o (da direita ou esquerda)
            long paginaIrmao;
            Pagina pIrmao;
            
            // Tenta a fus�o com irm�o esquerdo
            if(diminuido>0) {
                
                // Carrega o irm�o esquerdo
                paginaIrmao = pa.filhos[diminuido-1];
                pIrmao = new Pagina(ordem);
                arquivo.seek(paginaIrmao);
                arquivo.read(buffer);
                pIrmao.setBytes(buffer);
                
                // Testa se o irm�o pode ceder algum registro
                if(pIrmao.n>maxElementos/2) {
                    
                    // Move todos os elementos do filho aumentando uma posi��o
                    // � esquerda, gerando espa�o para o elemento cedido
                    for(int j=pFilho.n; j>0; j--) {
                        pFilho.chaves[j] = pFilho.chaves[j-1];
                        pFilho.dados[j] = pFilho.dados[j-1];
                        pFilho.filhos[j+1] = pFilho.filhos[j];
                    }
                    pFilho.filhos[1] = pFilho.filhos[0];
                    pFilho.n++;
                    
                    // Se for folha, copia o elemento do irm�o, j� que o do pai
                    // ser� extinto ou repetido
                    if(pFilho.filhos[0]==-1) {
                        pFilho.chaves[0] = pIrmao.chaves[pIrmao.n-1];
                        pFilho.dados[0] = pIrmao.dados[pIrmao.n-1];
                    }
                    
                    // Se n�o for folha, rotaciona os elementos, descendo o elemento do pai
                    else {
                        pFilho.chaves[0] = pa.chaves[diminuido-1];
                        pFilho.dados[0] = pa.dados[diminuido-1];
                    }

                    // Copia o elemento do irm�o para o pai (p�gina atual)
                    pa.chaves[diminuido-1] = pIrmao.chaves[pIrmao.n-1];
                    pa.dados[diminuido-1] = pIrmao.dados[pIrmao.n-1];
                        
                    
                    // Reduz o elemento no irm�o
                    pFilho.filhos[0] = pIrmao.filhos[pIrmao.n];
                    pIrmao.n--;
                    diminuiu = false;
                }
                
                // Se n�o puder ceder, faz a fus�o dos dois irm�os
                else {

                    // Se a p�gina reduzida n�o for folha, ent�o o elemento 
                    // do pai deve ser copiado para o irm�o
                    if(pFilho.filhos[0] != -1) {
                        pIrmao.chaves[pIrmao.n] = pa.chaves[diminuido-1];
                        pIrmao.dados[pIrmao.n] = pa.dados[diminuido-1];
                        pIrmao.filhos[pIrmao.n+1] = pFilho.filhos[0];
                        pIrmao.n++;
                    }
                    
                    
                    // Copia todos os registros para o irm�o da esquerda
                    for(int j=0; j<pFilho.n; j++) {
                        pIrmao.chaves[pIrmao.n] = pFilho.chaves[j];
                        pIrmao.dados[pIrmao.n] = pFilho.dados[j];
                        pIrmao.filhos[pIrmao.n+1] = pFilho.filhos[j+1];
                        pIrmao.n++;
                    }
                    pFilho.n = 0;   // aqui o endere�o do filho poderia ser incluido em uma lista encadeada no cabe�alho, indicando os espa�os reaproveit�veis
                    
                    // Se as p�ginas forem folhas, copia o ponteiro para a folha seguinte
                    if(pIrmao.filhos[0]==-1)
                        pIrmao.proxima = pFilho.proxima;
                    
                    // puxa os registros no pai
                    int j;
                    for(j=diminuido-1; j<pa.n-1; j++) {
                        pa.chaves[j] = pa.chaves[j+1];
                        pa.dados[j] = pa.dados[j+1];
                        pa.filhos[j+1] = pa.filhos[j+2];
                    }
                    pa.chaves[j] = 0;
                    pa.dados[j] = -1;
                    pa.filhos[j+1] = -1;
                    pa.n--;
                    diminuiu = pa.n<maxElementos/2;  // testa se o pai tamb�m ficou sem o n�mero m�nimo de elementos
                }
            }
            
            // Faz a fus�o com o irm�o direito
            else {
                
                // Carrega o irm�o
                paginaIrmao = pa.filhos[diminuido+1];
                pIrmao = new Pagina(ordem);
                arquivo.seek(paginaIrmao);
                arquivo.read(buffer);
                pIrmao.setBytes(buffer);
                
                // Testa se o irm�o pode ceder algum elemento
                if(pIrmao.n>maxElementos/2) {
                    
                    // Se for folha
                    if( pFilho.filhos[0]==-1 ) {
                    
                        //copia o elemento do irm�o
                        pFilho.chaves[pFilho.n] = pIrmao.chaves[0];
                        pFilho.dados[pFilho.n] = pIrmao.dados[0];
                        pFilho.filhos[pFilho.n+1] = pIrmao.filhos[0];
                        pFilho.n++;

                        // sobe o pr�ximo elemento do irm�o
                        pa.chaves[diminuido] = pIrmao.chaves[1];
                        pa.dados[diminuido] = pIrmao.dados[1];
                        
                    } 
                    
                    // Se n�o for folha, rotaciona os elementos
                    else {
                        
                        // Copia o elemento do pai, com o ponteiro esquerdo do irm�o
                        pFilho.chaves[pFilho.n] = pa.chaves[diminuido];
                        pFilho.dados[pFilho.n] = pa.dados[diminuido];
                        pFilho.filhos[pFilho.n+1] = pIrmao.filhos[0];
                        pFilho.n++;
                        
                        // Sobe o elemento esquerdo do irm�o para o pai
                        pa.chaves[diminuido] = pIrmao.chaves[0];
                        pa.dados[diminuido] = pIrmao.dados[0];
                    }
                    
                    // move todos os registros no irm�o para a esquerda
                    int j;
                    for(j=0; j<pIrmao.n-1; j++) {
                        pIrmao.chaves[j] = pIrmao.chaves[j+1];
                        pIrmao.dados[j] = pIrmao.dados[j+1];
                        pIrmao.filhos[j] = pIrmao.filhos[j+1];
                    }
                    pIrmao.filhos[j] = pIrmao.filhos[j+1];
                    pIrmao.n--;
                    diminuiu = false;
                }
                
                // Se n�o puder ceder, faz a fus�o dos dois irm�os
                else {

                    // Se a p�gina reduzida n�o for folha, ent�o o elemento 
                    // do pai deve ser copiado para o irm�o
                    if(pFilho.filhos[0] != -1) {
                        pFilho.chaves[pFilho.n] = pa.chaves[diminuido];
                        pFilho.dados[pFilho.n] = pa.dados[diminuido];
                        pFilho.filhos[pFilho.n+1] = pIrmao.filhos[0];
                        pFilho.n++;
                    }
                    
                    // Copia todos os registros do irm�o da direita
                    for(int j=0; j<pIrmao.n; j++) {
                        pFilho.chaves[pFilho.n] = pIrmao.chaves[j];
                        pFilho.dados[pFilho.n] = pIrmao.dados[j];
                        pFilho.filhos[pFilho.n+1] = pIrmao.filhos[j+1];
                        pFilho.n++;
                    }
                    pIrmao.n = 0;   // aqui o endere�o do irm�o poderia ser incluido em uma lista encadeada no cabe�alho, indicando os espa�os reaproveit�veis
                    
                    // Se a p�gina for folha, copia o ponteiro para a pr�xima p�gina
                    pFilho.proxima = pIrmao.proxima;
                    
                    // puxa os registros no pai
                    for(int j=diminuido; j<pa.n-1; j++) {
                        pa.chaves[j] = pa.chaves[j+1];
                        pa.dados[j] = pa.dados[j+1];
                        pa.filhos[j+1] = pa.filhos[j+2];
                    }
                    pa.n--;
                    diminuiu = pa.n<maxElementos/2;  // testa se o pai tamb�m ficou sem o n�mero m�nimo de elementos
                }
            }
            
            // Atualiza todos os registros
            arquivo.seek(pagina);
            arquivo.write(pa.getBytes());
            arquivo.seek(paginaFilho);
            arquivo.write(pFilho.getBytes());
            arquivo.seek(paginaIrmao);
            arquivo.write(pIrmao.getBytes());
        }
        return excluido;
    }
    
    
    // Imprime a �rvore, usando uma chamada recursiva.
    // A fun��o recursiva � chamada com uma p�gina de refer�ncia (raiz)
    public void print() throws IOException {
        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();
        if(raiz!=-1)
            print1(raiz);
        System.out.println();
    }
    
    // Impress�o recursiva
    private void print1(long pagina) throws IOException {
        
        // Retorna das chamadas recursivas
        if(pagina==-1)
            return;
        int i;

        // L� o registro da p�gina passada como refer�ncia no arquivo
        arquivo.seek(pagina);
        Pagina pa = new Pagina(ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.setBytes(buffer);
        
        // Imprime a p�gina
        String endereco = String.format("%04d", pagina);
        System.out.print(endereco+"  " + pa.n +":"); // endere�o e n�mero de elementos
        for(i=0; i<maxElementos; i++) {
            System.out.print("("+String.format("%04d",pa.filhos[i])+") "+String.format("%2d",pa.chaves[i])+","+String.format("%2d",pa.dados[i])+" ");
        }
        System.out.print("("+String.format("%04d",pa.filhos[i])+")");
        if(pa.proxima==-1)
            System.out.println();
        else
            System.out.println(" --> ("+String.format("%04d", pa.proxima)+")");
        
        // Chama recursivamente cada filho, se a p�gina n�o for folha
        if(pa.filhos[0] != -1) {
            for(i=0; i<pa.n; i++)
                print1(pa.filhos[i]);
            print1(pa.filhos[i]);
        }
    }
       
    
    // Apaga o arquivo do �ndice, para que possa ser reconstru�do
    public void apagar() throws IOException {

        File f = new File(nomeArquivo);
        f.delete();

        arquivo = new RandomAccessFile(nomeArquivo,"rw");
        arquivo.writeLong(-1);  // raiz vazia
    }
    
}
