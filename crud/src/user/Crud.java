package user;

import java.io.*;
import crud.*;
import java.util.*;

public class Crud 
{   
   /**
    * Metodo para criar um novo registro de produto
    * @param - arquivo destino
    *
    */
   public static void inserir(Arquivo arquivo)
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
	      
	  }catch(IOException ioe) {ioe.printStackTrace(); }
	  
   }//end inserir()
   
   
   public static void alterar(Arquivo arquivo, int id)
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
	      
	  }catch(IOException ioe) {ioe.printStackTrace(); }
	  
   }//end alterar()
   
   
   public static void main(String [] args) throws IOException
   {
      try
      {
         //definir dados
         BufferedReader br = new BufferedReader( new InputStreamReader(System.in) );
         //File file = new File("produto.db");
         Arquivo arquivo = new Arquivo("produtos.db");
         //ArrayList<Produto> list = new ArrayList<Produto>();
         int selecao = 12;
         System.out.println("Ol�, meu nobre!\n");
         
      //TESTAR SE E' PARA SAIR
         while(selecao != 0)
         {    
            //Interface de entrada
            System.out.println("Qual das seguintes opera��es o senhor deseja realizar?" + 
                            "\nDigite: " + 
                            "\n1 para inclus�o;" +
                            "\n2 para altera��o;" +
                            "\n3 para exclus�o;" +
                            "\n4 para consulta de produtos;" +
                            "\n0 para sair.");
                             
            selecao = Integer.parseInt( br.readLine() ); //entrada do codigo de selecao de acao         
             
            //INCLUSAO
            if     (selecao == 1){ inserir(arquivo);  }
            else if(selecao == 2)
            { 
            	int id = 0;
            	System.out.println("Digite o id do produto a ser alterado: ");
            	id = Integer.parseInt( br.readLine() );
            	System.out.println("O que deseja alterar no produto?\nDigite:\n");
            	System.out.println("1 para alterar o nome;");
            	System.out.println("1 para alterar a descri��o;");
            	System.out.println("1 para alterar o pre�o;");            
            }
           // else if(selecao == 3){ remover(arquivo);  } 
           // else if(selecao == 4){ consultar(arquivo);}
            
         }//end while
         
         System.out.println("At� breve :)");
      } 
      catch(IOException ioe){ ioe.printStackTrace(); }   
         
   	/*
      ArrayList<Produto> list = new ArrayList<Produto>();
   	
      
      Produto produto = new Produto("Geladeira", "Geladeira Duplex FrostFree 30kg", (float)1200.00);
      Produto produto1 = new Produto("Ps4", "500gb HD", (float)1800.00);
      Produto produto2 = new Produto("TV", "4k Full HD Master", (float)1800.00);
   	
      
      arquivo.writeObject(produto1);
      arquivo.writeObject(produto2);
   	
      System.out.println(arquivo.readObject(1));
   	
      list = arquivo.list();
   	
      for(int i=0; i<list.size(); i++) {
         System.out.println(list.get(i));
      }
      */
   
   }//end main()
   
}//end class Main
