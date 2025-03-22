import { useState } from "react";
import './../css/ListWithArrows.css'
import './../css/CommonElements.css'
import { useNavigate } from "react-router-dom";

export interface ListItem {
  id: number | string;
  image: string;
  title: string;
  description: string;
}

export interface ListProps {
  data: ListItem[];
  limit: number;
  navigateTo: string;
}

const List: React.FC<ListProps> = ({ data, limit, navigateTo }) => {
  const navigate = useNavigate();
  const [startIndex, setStartIndex] = useState(0); 

  const handlePrev = () => {
    if(startIndex == 0) {
      setStartIndex(data.length - 1); 
    }
    else {
      setStartIndex(startIndex - 1);
    }
  };

  const handleNext = () => {
    if(startIndex == data.length - 1) {
      setStartIndex(0); 
    }
    else {
      setStartIndex(startIndex + 1);
    }
  };

  const handleTitleOnClick = (postId: string | number) => {
    navigate(`/${navigateTo}/${postId}`);
  }

  const visibleItems1 = data.slice(startIndex, startIndex + limit);
  const visibleItems2 = data.slice(0, limit - visibleItems1.length);
  const visibleItems = visibleItems1.concat(visibleItems2);

  return (
    <div className="listComponent">
      <link
      href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
      rel="stylesheet"
      />

      <button onClick={handlePrev} className="leftArrow"></button>

      <div className="list">
        {visibleItems.map((item) => (
          <div key={item.id} className="listItem" onClick={() => handleTitleOnClick(item.id)}>
            <img
              src={item.image}
              alt={item.title}
              className="listItemImg"
            />
            <h3 className="listItemHeader">{item.title}</h3>
            <p className="listItemDesc">{item.description}</p>
          </div>
        ))}
      </div>

      <button onClick={handleNext} className="rightArrow"></button>


    </div>
  );
};

export default List;