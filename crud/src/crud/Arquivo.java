package crud;

import java.io.*;

import java.util.ArrayList;

//colocar no array list e fazer o index para buscar por nome 

public class Arquivo {
	private String name;
	public short lastID;
	RandomAccessFile accessFile;

	public Arquivo(String nameFile) {
		this.name = nameFile;
		this.lastID = -1;
	}

	public short getLastID() {
		return this.lastID;
	}

	private short setLastID(short lastID) {
		return this.lastID = lastID;
	}
	
	/**
	 * Abre o arquivo da classe.
	 * 
	 * @return o arquivo da classe.
	 */
	
	protected RandomAccessFile openFile()
	{
		RandomAccessFile file = null;
		
		try
		{
			file = new RandomAccessFile(name, "rw");
			file.seek(0);
		}
		
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return file;
	}
	
	/**
	 * L� o �ltimo ID usado pelo cabe�alho do arquivo.
	 * 
	 * Obs.: pressup�e-se que os dois primeiros bytes do arquivo s�o o
	 * short que guarda o �ltimo ID usado.
	 */
	
	private short readLastID()
	{
		RandomAccessFile file = openFile();
		short lastID = -1;
		
		try
		{
			lastID = file.readShort();
			file.close();
		}
		
		catch (IOException IOEx) { }
		
		return ( lastID == -1 ? this.lastID : lastID );
	}
	
	/**
	 * Escreve {@code lastID} no cabecalho do arquivo.
	 * 
	 * @param lastID novo valor para o �ltimo ID
	 * 
	 * @return {@code lastID}
	 */
	
	private short writeLastID(short lastID)
	{
		RandomAccessFile file = openFile();
		
		try
		{
			file.writeShort(lastID);
			file.close();
		}
		
		catch (IOException IOEx) { }
		
		return lastID;
	}
	
	public void writeObject(Produto produto) throws IOException {
		try {
			accessFile = openFile();
			
			produto.setId( (short) (readLastID() + 1) );
			setLastID( writeLastID( produto.getId() ) );
			
			byte[] byteArray = produto.setByteArray();

			// go to final of file
			accessFile.seek(accessFile.length());
			
			accessFile.writeShort(produto.getId()); // write id
			accessFile.writeChar(' '); //lapide
			accessFile.writeInt(byteArray.length);
			accessFile.write(byteArray);

			accessFile.close();
		} catch (FileNotFoundException fNFE) {
			fNFE.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * M�todo para listar todo o arquivo [EM CONSTRU��O]
	 * @return ArrayList
	 * @throws IOException
	 */
	public ArrayList<Produto> list() throws IOException{
        ArrayList<Produto> listProdutos = new ArrayList<Produto>();
		Produto produto = new Produto();
        accessFile = openFile();

        int gap = 0;

       // byte[] byteArray = new byte[accessFile.readInt()]; // byteArray inutil por enquanto mas pretendo implementá-lo

        accessFile.seek(2);

        while(accessFile.getFilePointer() < accessFile.length()){
            short id = accessFile.readShort(); 
            if(accessFile.readChar() != '*') {
            	 gap = accessFile.readInt();
                 byte[] byteArray = new byte[gap];
                 
                 produto.fromByteArray(byteArray, id);
                 listProdutos.add(produto);
            } else {
            	gap = accessFile.readInt();
            }
           
            System.out.println("Pos: " + accessFile.getFilePointer());
            accessFile.seek(accessFile.getFilePointer() + gap);
        }
       
        return listProdutos;
    }
 
	public Produto readObject(int id) {
		Produto produto = new Produto();
		int gap = 0;

		try {
			accessFile = openFile();

			accessFile.seek(2);

			while (accessFile.getFilePointer() < accessFile.length()) {
				short thisId = accessFile.readShort();
				if(accessFile.readChar() != '*' && thisId == id) {
					byte[] byteArray = new byte[accessFile.readInt()];
					accessFile.read(byteArray);

					produto.fromByteArray(byteArray, thisId);

					accessFile.seek(accessFile.length());
				} else {
					gap = accessFile.readInt();
					accessFile.seek(accessFile.getFilePointer() + gap);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return produto;
	}
	
	//estrutura do vetor de byte
	// ultimo id usado - [id] - [lapide] - [tamanho da entidade] -> entidade
}