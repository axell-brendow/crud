/*
MIT License

Copyright (c) 2019 Axell Brendow Batista Moreira

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package serializaveis;

/**
 * Interface para padronização da forma de serializar objetos.
 * 
 * @author Axell Brendow ( https://github.com/axell-brendow )
 * 
 * @see <a href="https://pt.wikipedia.org/wiki/Serializa%C3%A7%C3%A3o">
 * Serialização – Wikipédia, a enciclopédia livre</a>
 */

public interface Serializavel
{
	/**
	 * Calcula a quantidade máxima de bytes que o
	 * objeto pode gastar.
	 * 
	 * @return a quantidade máxima de bytes que o
	 * objeto pode gastar.
	 */
	
	int obterTamanhoMaximoEmBytes();
	
	/**
	 * Gera um vetor com os bytes do objeto.
	 * 
	 * @return um vetor com os bytes do objeto.
	 */
	
	byte[] obterBytes();
	
	/**
	 * Lê os bytes do vetor atribuindo os campos internos da
	 * entidade.
	 * 
	 * @param bytes Vetor com os bytes do objeto.
	 */
	
	public void lerBytes(byte[] bytes);
	
	/**
	 * Gera uma string com base nos campos internos da entidade.
	 * 
	 * @return uma string com base nos campos internos da entidade.
	 */
	
	public String toString();
}
