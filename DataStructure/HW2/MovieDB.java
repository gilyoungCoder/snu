import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Genre, Title 을 관리하는 영화 데이터베이스.
 *
 * MyLinkedList 를 사용해 각각 Genre와 Title에 따라 내부적으로 정렬된 상태를
 * 유지하는 데이터베이스이다.
 */
public class MovieDB {

	MyLinkedList<MyLinkedList<MovieDBItem>> gmlist = new MyLinkedList<MyLinkedList<MovieDBItem>>();
	int place = 0;

	public MovieDB() {
	//	this.gmlist.numItems = 0;
	}

	public void insert(MovieDBItem item) {
		MyLinkedList<MovieDBItem> itemlist = new MyLinkedList<MovieDBItem>();
		itemlist.add(item);
		MyLinkedListIterator<MyLinkedList<MovieDBItem>> dgmlist = new MyLinkedListIterator<MyLinkedList<MovieDBItem>>(gmlist);

		if(gmlist.size() == 0) {
			gmlist.add(itemlist);
			return;
		}

		else {
			while(dgmlist.hasNext()) {
				place = item.getGenre().compareTo(dgmlist.next().head.getNext().getItem().getGenre());
				if(place>0) {
					if(!dgmlist.hasNext()) {
						gmlist.add(itemlist);
						return;
					}
				}

				else if(place < 0) {
					dgmlist.insert(itemlist);
					return;
				}

				else {
					break;
				}
			}

			if(place == 0) {
				MyLinkedListIterator<MovieDBItem> dmlist = new MyLinkedListIterator<MovieDBItem> (dgmlist.currItem());

				while(dmlist.hasNext()) {
					place = item.getTitle().compareTo(dmlist.next().getTitle());
					if(place>0) {
						if(!dmlist.hasNext()) {
							dmlist.insertlast(item);
							return;
						}
					}

					else if(place < 0) {
						dmlist.insert(item);
						return;
					}

					else {
						return;
					}

				}
			}
		}


	}

	public void delete(MovieDBItem item) {

		MyLinkedListIterator<MyLinkedList<MovieDBItem>> dgmlist = new MyLinkedListIterator<MyLinkedList<MovieDBItem>>(gmlist);

		while(dgmlist.hasNext()) {

			if(item.getGenre().equals(dgmlist.next().head.getNext().getItem().getGenre())) {
				break;
			}
		}

		if(item.getGenre().equals(dgmlist.currItem().head.getNext().getItem().getGenre())) {
			MyLinkedListIterator<MovieDBItem> dmlist = new MyLinkedListIterator<MovieDBItem> (dgmlist.currItem());

			while(dmlist.hasNext()) {
				if(item.getTitle().equals(dmlist.next().getTitle())) {
					dmlist.remove();
					if(dgmlist.currItem().size()==0) {
						dgmlist.remove();
					}
					return;
				}
			}

		}
	}

	public MyLinkedList<MovieDBItem> search(String term) {

		MyLinkedList<MovieDBItem> results = new MyLinkedList<MovieDBItem>();
		MyLinkedListIterator<MyLinkedList<MovieDBItem>> dgmlist = new MyLinkedListIterator<MyLinkedList<MovieDBItem>>(gmlist);

		while(dgmlist.hasNext()) {
			MyLinkedListIterator<MovieDBItem> dmlist = new MyLinkedListIterator<MovieDBItem> (dgmlist.next());
			while(dmlist.hasNext()) {
				if(dmlist.next().getTitle().contains(term))
					results.add(dmlist.currItem());
			}

		}

		return results;
	}

	public MyLinkedList<MovieDBItem> items() {
	

		MyLinkedListIterator<MyLinkedList<MovieDBItem>> dgmlist = new MyLinkedListIterator<MyLinkedList<MovieDBItem>>(gmlist);
		MyLinkedList<MovieDBItem> mlist = new MyLinkedList<MovieDBItem>();

		while(dgmlist.hasNext()) {
			MyLinkedListIterator<MovieDBItem> dmlist = new MyLinkedListIterator<MovieDBItem> (dgmlist.next());
			while(dmlist.hasNext()) {
				mlist.add(dmlist.next());
			}

		}


		return mlist;


	}
}


class Genre extends Node<String> implements Comparable<Genre> {
	public Genre(String name) {
		super(name);
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public int compareTo(Genre o) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException("not implemented yet");
	}
}

class MovieList implements ListInterface<String> {
	public MovieList() {
	}

	@Override
	public Iterator<String> iterator() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void add(String item) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public String first() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void removeAll() {
		throw new UnsupportedOperationException("not implemented yet");
	}
}

 