package user;

import java.io.*;
import java.util.ArrayList;

import crud.Produto;
import crud.Arquivo;

public class Crud
{   
   /**
    * Metodo para criar um novo registro de produto
    * @param - arquivo destino
    *
    */
   public static void inserir(Arquivo arquivo) throws IOException
   {  
      try 
      {
         BufferedReader br = new BufferedReader( new InputStreamReader(System.in) );
         String nome, descricao;
         float preco = 0.0F;
            
         System.out.println("\nDigite o nome do produto: ");
         nome = br.readLine();
               
         System.out.println("\nDigite a descri��o do produto: ");
         descricao = br.readLine();
               
         System.out.println("\nInforme o pre�o do produto: ");
         preco = Float.parseFloat( br.readLine() );
               
         Produto produto = new Produto(nome, descricao, preco); 
                  
         arquivo.writeObject(produto);
         
         System.out.println("Seu produto foi cadastrado com sucasso! :D\n");
         
      }
      catch(IOException ioe) {ioe.printStackTrace(); }
     
   }//end inserir()
   
   
   /**
    * M�todo para alterar dados do produto(exceto o id).
    * [EM CONSTRU��O]
    * @param arquivo
    * @param id
    * @param cod
    */
   public static void alterar(Arquivo arquivo, int id, short cod) throws IOException
   {  	  
      try 
      {
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         Produto produto = new Produto();
         produto = arquivo.readObject(id);	   	   
      	   
      	   //Bloco de if's para alteracao de atributos
         if(cod == 1)
         { 	   		   	   		      
            System.out.println("Digite o novo nome do produto: ");
            produto.setNome( br.readLine() ); //Setar novo nome
         }
         else if(cod == 2) 
         {
            System.out.println("Digite a nova descri��o do produto: ");
            produto.setDescricao( br.readLine() ); //Setar nova descricao
         }
         else if(cod == 3) 
         {
            System.out.println("Digite o novo pre�o do produto: ");
            produto.setPreco( Float.parseFloat( br.readLine() ) ); //Setar novo preco
         }//end if
      	   	   	   
      	   
      }
      catch(IOException ioe) { ioe.printStackTrace(); }
      
   }//end alterar()
   
   
   public static void listar()
   {
      
   }//end listar()
   
   
   public static void main(String [] args) throws IOException
   {
      try
      {
         //definir dados    	 
         BufferedReader br = new BufferedReader( new InputStreamReader(System.in) );
         //File file = new File("produto.db");
         Arquivo arquivo = new Arquivo("produtos.db");
         ArrayList<Produto> list = new ArrayList<Produto>();
         int selecao = 12;
         System.out.println("Ol�, meu nobre!\n");
         
         //Interface de entrada
         System.out.println("Qual das seguintes opera��es o senhor deseja realizar?" + 
                         "\nDigite: " + 
                         "\n1 para inclus�o;" +
                         "\n2 para altera��o;" +
                         "\n3 para exclus�o;" +
                         "\n4 para consulta de produtos;" +
                         "\n5 para listar todos os produtos" +
                         "\n0 para sair.");
         
      //TESTAR SE E' PARA SAIR
         while(selecao != 0)
         {                                          
            selecao = Integer.parseInt( br.readLine() ); //entrada do codigo de selecao de acao         
         
            if     (selecao == 1){ inserir(arquivo);  }// INCLUSAO
            else if(selecao == 2)// ALTERACAO
            { 
               int id = 0;
               short cod = -1; //codigo de selecao
            	
               System.out.println("Digite o id do produto a ser alterado: ");
               id = Integer.parseInt( br.readLine() );
            	
            	//testar antes se o id existe
               if(id > 0 && id == arquivo.getLastID())
               {
                  System.out.println("O que deseja alterar no produto?\nDigite:");
                  System.out.println("1 para alterar o nome;");
                  System.out.println("2 para alterar a descri��o;");
                  System.out.println("3 para alterar o pre�o;");
                  System.out.println("0 para cancelar;");
               	
                  cod = Short.parseShort( br.readLine() );
               	
                  alterar(arquivo, id, cod);
               	
               }
               else { System.out.println("Id inv�lido!"); }
            	            	
            	
            }
            // else if(selecao == 3){ remover(arquivo);  } 
            // else if(selecao == 4){ consultar(arquivo);}
            else if(selecao == 5) 
            {
               list = arquivo.list();
            
               for(int i=0; i<list.size(); i++) {
                  System.out.println(list.get(i));
               }//end for
            
            }//end if
            
         }//end while
         
         System.out.println("At� breve :)");
         
      } 
      catch(IOException ioe){ ioe.printStackTrace(); }   
         
      //arquivo.delete();
      
   }//end main()
   
}//end class Main
