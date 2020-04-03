## Few tips how to use this module
Unfortunately I had some probelems with venv, so I can't provide adequate requirements.txt

Basecally, all you need is to run 
- `pip3 install matplotlib`
- `pip3 install opencv-python`
- `pip3 install tensorflow`
- `pip3 install mtcnn`

**Example of using with explanation:**
```
import first_example


identifier1 = Identifier()  # create instance of provided class 
filename = "test_data/" + input("Type picture name (from test_data, with .jpg): ")  # ask user for img name(from ./test_data)
identifier1.filename = filename  # set img name for our instance
face_results = identifier1.find_faces()  # get face locations
identifier1.show_faces()  # show faces in window
identifier1.save_faces()  # save face locations to result.json
identifier1.web_camera_stream()  # Identificate faces through our web cam
```

For more info, look at the docunentation inside file.
