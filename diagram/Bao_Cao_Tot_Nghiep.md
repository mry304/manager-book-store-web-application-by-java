# LỜI NÓI ĐẦU

Trong bối cảnh công nghệ số ngày càng gắn liền với hoạt động kinh doanh và nhu cầu tiếp cận tri thức, việc ứng dụng công nghệ thông tin vào quản lý và mua bán sách có ý nghĩa thiết thực. Một hệ thống web phù hợp không chỉ hỗ trợ cửa hàng giới thiệu sản phẩm đến người đọc, mà còn giúp tổ chức dữ liệu sách, quản lý giao dịch, theo dõi đơn hàng và cung cấp thông tin phục vụ điều hành. Đối với người dùng, nền tảng trực tuyến tạo điều kiện tìm kiếm, lựa chọn và mua sách thuận tiện hơn; đối với đơn vị kinh doanh, hệ thống góp phần giảm các thao tác quản lý thủ công và nâng cao khả năng theo dõi hoạt động bán hàng.

Xuất phát từ yêu cầu đó, nhóm lựa chọn thực hiện đề tài **“Phát triển ứng dụng web quản lý mua bán sách”**. Đề tài hướng đến xây dựng hệ thống hỗ trợ quy trình kinh doanh sách trực tuyến, bao gồm quản lý tài khoản, danh mục sách, giỏ hàng, đơn hàng, thanh toán VietQR, quản lý kho và thống kê. Bên cạnh các nghiệp vụ cốt lõi, hệ thống được định hướng mở rộng theo hướng nâng cao trải nghiệm người đọc thông qua trợ lý AI tư vấn và tóm tắt sách, đánh giá sách, kệ sách ảo, thử thách đọc; đồng thời phát triển mô hình ký gửi sách cũ và Book Coins nhằm hỗ trợ lưu chuyển giá trị của sách trong cộng đồng.

Quá trình thực hiện đề tài là cơ hội để nhóm vận dụng các kiến thức đã học về khảo sát yêu cầu, phân tích và thiết kế hệ thống, mô hình hóa dữ liệu, phát triển ứng dụng web và tích hợp dịch vụ bên ngoài. Báo cáo không chỉ mô tả kết quả xây dựng hệ thống mà còn trình bày cơ sở phân tích các yêu cầu, các quyết định thiết kế và định hướng phát triển tiếp theo. Những nội dung này giúp làm rõ mối liên hệ giữa bài toán quản lý mua bán sách trong thực tế với giải pháp phần mềm được đề xuất.

Nội dung báo cáo được tổ chức thành các phần chính. Chương I giới thiệu tổng quan đề tài, mục tiêu, đối tượng, phạm vi nghiên cứu và yêu cầu hệ thống. Chương II trình bày hoạt động phân tích và thiết kế, bao gồm các mô hình use case, luồng nghiệp vụ, kiến trúc, cơ sở dữ liệu và giao diện. Phần kết luận tổng hợp kết quả đạt được, những hạn chế còn tồn tại và hướng phát triển của hệ thống trong thời gian tới.

Nhóm xin chân thành cảm ơn giảng viên hướng dẫn và các thầy cô đã truyền đạt kiến thức, góp ý trong quá trình học tập và thực hiện đồ án. Do thời gian, kinh nghiệm và nguồn lực thực hiện còn hạn chế, báo cáo khó tránh khỏi những thiếu sót. Nhóm rất mong nhận được các ý kiến đóng góp để tiếp tục hoàn thiện đề tài.

# CHƯƠNG I: TỔNG QUAN VỀ ĐỀ TÀI

## 1.1. Giới thiệu đề tài

Đề tài **“Phát triển ứng dụng web quản lý mua bán sách”** tập trung xây dựng một hệ thống web hỗ trợ toàn bộ quá trình kinh doanh sách, từ tiếp cận thông tin sách, lựa chọn và thanh toán của khách hàng đến quản lý kho, đơn hàng và theo dõi hoạt động kinh doanh của cửa hàng. Hệ thống được định hướng là một không gian số kết nối người đọc với đơn vị kinh doanh sách, trong đó dữ liệu về sách, tài khoản, giao dịch và hoạt động quản trị được quản lý tập trung thay vì xử lý rời rạc.

Đối tượng phục vụ chính của hệ thống là độc giả cá nhân, học sinh, sinh viên và những người có nhu cầu tìm kiếm, mua hoặc trao đổi sách. Với nhóm người dùng này, việc lựa chọn sách không chỉ dựa trên danh mục hay giá bán mà còn có thể xuất phát từ sở thích, mục tiêu đọc và nhu cầu tại thời điểm truy cập. Bên cạnh đó, hệ thống phục vụ người bán hoặc quản trị viên trong việc cập nhật danh mục sách, kiểm soát tình trạng hàng hóa, theo dõi đơn mua, xử lý các yêu cầu ký gửi và khai thác thông tin hỗ trợ điều hành.

Về mô hình hoạt động, hệ thống kết hợp bán sách mới trực tiếp cho khách hàng với định hướng phát triển mô hình ký gửi sách đã qua sử dụng. Ở mô hình bán sách trực tiếp, khách hàng có thể tạo tài khoản, tìm kiếm sách, đưa sách vào giỏ hàng, đặt hàng và thanh toán trực tuyến. Ở mô hình ký gửi, người dùng có thể gửi thông tin sách cũ để hệ thống hoặc quản trị viên xem xét trước khi đưa sách lên danh mục phù hợp. Sự kết hợp này mở rộng vai trò của ứng dụng từ một cửa hàng trực tuyến thông thường thành nền tảng hỗ trợ lưu chuyển giá trị của sách trong cộng đồng người đọc.

Đề tài đặt trọng tâm vào việc hình thành trải nghiệm mua sách có tính liên tục và minh bạch. Thông tin cơ bản về sách, giá bán và tình trạng sẵn có cần được cung cấp để khách hàng có cơ sở lựa chọn. Khi phát sinh nhu cầu mua, khách hàng có thể quản lý các sách đã chọn trong giỏ hàng, hoàn tất thông tin giao nhận và thực hiện thanh toán qua mã VietQR. Kết quả giao dịch được dùng làm căn cứ để xác nhận đơn hàng và thông báo cho các bên liên quan. Cách tổ chức này hướng đến giảm thao tác thủ công trong quá trình mua bán và giúp cửa hàng quản lý giao dịch có hệ thống hơn.

Một điểm định hướng của đề tài là nâng cao khả năng hỗ trợ người đọc. Thay vì chỉ cung cấp danh mục theo các nhóm chủ đề cố định, hệ thống dự kiến tích hợp trợ lý AI để tiếp nhận truy vấn bằng ngôn ngữ tự nhiên, gợi ý sách theo nhu cầu hoặc cảm xúc và hỗ trợ tạo nội dung tóm tắt. Các chức năng đánh giá, kệ sách ảo và thử thách đọc cũng được định hướng nhằm giúp người dùng lưu lại trải nghiệm đọc, theo dõi mục tiêu cá nhân và có thêm cơ sở khi chọn sách. Những thành phần này được đặt trong mối quan hệ với chức năng mua bán: gợi ý và thông tin đọc hỗ trợ lựa chọn, còn dữ liệu sách là nền tảng để hình thành trải nghiệm cá nhân hóa.

Đối với công tác quản trị, hệ thống hướng đến cung cấp công cụ quản lý danh mục, số lượng sách, đơn hàng và báo cáo kinh doanh. Người quản trị có thể theo dõi các thông tin phát sinh từ hoạt động mua bán để xử lý đơn, cập nhật kho và nhận biết các vấn đề cần quan tâm trong vận hành. Theo định hướng của hồ sơ đặc tả, hệ thống còn có trung tâm thông báo và các biểu đồ tổng hợp nhằm hỗ trợ theo dõi sự kiện, doanh thu và tình hình hàng hóa.

Ứng dụng được phát triển theo hướng web đa nền tảng, có khả năng sử dụng trên máy tính để bàn, máy tính bảng và điện thoại di động. Nền tảng kỹ thuật được lựa chọn là Java với kiến trúc ứng dụng web, cơ sở dữ liệu quan hệ và các dịch vụ tích hợp cho thanh toán, thư điện tử và trí tuệ nhân tạo. Các thành phần kỹ thuật, cấu trúc dữ liệu và cơ chế xử lý nghiệp vụ sẽ được phân tích cụ thể ở các chương sau; trong phạm vi phần giới thiệu, chúng được xem là phương tiện để hiện thực hóa mục tiêu quản lý và kinh doanh sách của đề tài.

Như vậy, đề tài không chỉ hướng đến số hóa việc bán sách, mà còn định hướng tạo ra một hệ sinh thái đọc và trao đổi sách có khả năng mở rộng. Hệ thống kết nối nhu cầu tìm hiểu, mua và sử dụng sách của độc giả với yêu cầu quản lý, vận hành của cửa hàng; từ đó tạo cơ sở để phát triển các dịch vụ giá trị gia tăng trong tương lai.

### 1.1.1. Lý do chọn đề tài

Sự phát triển của môi trường số làm thay đổi cách người dùng tiếp cận và mua sắm sách. Người đọc có thể tìm thông tin, so sánh nhiều đầu sách và đặt mua mà không cần trực tiếp đến cửa hàng. Tuy nhiên, một website chỉ hiển thị sản phẩm chưa đủ để giải quyết đầy đủ nhu cầu thực tế. Người mua cần biết sách có phù hợp với mình hay không, còn khả dụng hay không, tổng chi phí của đơn mua là bao nhiêu và trạng thái thanh toán được xác nhận như thế nào. Cửa hàng lại cần quản lý chính xác số lượng sách, thông tin khách hàng, giao dịch và các công việc phát sinh sau khi khách đặt hàng. Những nhu cầu đó tạo ra bài toán cần được giải quyết bằng một hệ thống quản lý mua bán sách có tổ chức.

Lý do đầu tiên lựa chọn đề tài là đặc thù của sản phẩm sách. Sách có số lượng đầu mục phong phú và mỗi đầu sách có các thông tin riêng như tên, tác giả, giá bán, tình trạng hàng hóa và nội dung chủ đề. Việc tìm kiếm sách theo các danh mục cố định thuận tiện cho quản lý ban đầu nhưng có thể chưa đáp ứng tốt nhu cầu đa dạng của người đọc. Một người muốn tìm tài liệu phục vụ học tập, tìm một cuốn sách thư giãn, hoặc tìm sách theo định hướng phát triển bản thân có thể diễn đạt nhu cầu bằng những cách khác nhau. Vì vậy, hệ thống cần hướng tới cách hỗ trợ khám phá sách linh hoạt hơn, trong đó trợ lý AI là định hướng phù hợp để tiếp nhận nhu cầu bằng ngôn ngữ tự nhiên và gợi ý lựa chọn có liên quan.

Lý do thứ hai là yêu cầu cải thiện trải nghiệm và độ tin cậy của quá trình đặt hàng. Trong phương thức bán hàng thủ công, khách hàng có thể phải gửi danh sách sách cần mua qua nhiều kênh, chờ phản hồi về tồn kho, sau đó tự chuyển khoản và cung cấp bằng chứng thanh toán. Cửa hàng cũng phải dành thời gian đối chiếu thông tin chuyển khoản với đơn mua trước khi xác nhận. Quy trình này có nguy cơ gây chậm trễ, nhầm lẫn về số tiền hoặc khó khăn khi tra cứu lại giao dịch. Việc đưa giỏ hàng, thông tin giao nhận, thanh toán VietQR và xác nhận giao dịch vào cùng một quy trình giúp xác định rõ các bước cần thực hiện, đồng thời tạo cơ sở để dữ liệu đơn hàng được quản lý nhất quán.

Lý do thứ ba là nhu cầu quản trị gắn liền với mỗi giao dịch bán sách. Khi khách hàng đặt một đơn mua, cửa hàng không chỉ cần tiếp nhận yêu cầu mà còn phải kiểm tra sách, theo dõi trạng thái xử lý, cập nhật thông tin hàng hóa và phản hồi cho khách hàng. Nếu các công việc này được thực hiện trên sổ sách hoặc các bảng tính riêng lẻ, việc tổng hợp tình hình bán hàng, kiểm soát tồn kho và phát hiện các đơn cần xử lý sẽ mất nhiều thời gian. Một ứng dụng quản lý tập trung cho phép thông tin về sách, khách hàng và đơn mua có quan hệ rõ ràng, từ đó hỗ trợ quản trị viên xử lý công việc dựa trên dữ liệu thay vì phụ thuộc vào việc ghi nhớ hay đối chiếu thủ công.

Lý do thứ tư xuất phát từ giá trị sử dụng lâu dài của sách. Sau khi đọc xong, nhiều cuốn sách vẫn còn khả năng sử dụng nhưng không còn được khai thác, trong khi một bộ phận người đọc có nhu cầu tiếp cận sách với chi phí thấp hơn. Định hướng ký gửi sách cũ tạo ra cơ chế để người dùng đưa sách đã qua sử dụng trở lại vòng lưu thông, còn hệ thống đóng vai trò tiếp nhận, kiểm duyệt và kết nối người có sách với người có nhu cầu mua. Kết hợp với cơ chế quy đổi giá trị thành Book Coins theo hồ sơ đặc tả, mô hình này hướng đến khuyến khích tái sử dụng sách và mở rộng giá trị của nền tảng vượt ra ngoài hoạt động bán sách mới.

Lý do thứ năm là mong muốn xây dựng một hệ thống gần gũi hơn với hành trình đọc của người dùng. Việc mua sách chỉ là điểm bắt đầu; người đọc còn có nhu cầu lưu lại sách muốn đọc, theo dõi tiến độ, đặt mục tiêu và tham khảo nhận xét từ cộng đồng. Kệ sách ảo, thử thách đọc và chức năng đánh giá được định hướng để đáp ứng các nhu cầu này. Khi được kết nối với danh mục và hoạt động mua bán, các chức năng trên có thể giúp hệ thống trở thành không gian hỗ trợ đọc sách thay vì chỉ là nơi hoàn tất giao dịch.

Về phương diện học thuật, đề tài phù hợp với mục tiêu của học phần Đồ án chuyên ngành Công nghệ phần mềm vì có bài toán nghiệp vụ rõ ràng, nhiều nhóm người dùng và các thành phần có quan hệ chặt chẽ. Việc thực hiện đề tài tạo điều kiện vận dụng kiến thức khảo sát yêu cầu, phân tích tác nhân, mô hình hóa quy trình, thiết kế dữ liệu, xây dựng giao diện web, phân quyền và tích hợp dịch vụ bên ngoài. Quan trọng hơn, các nội dung này cần được xem xét như một hệ thống thống nhất: trải nghiệm của khách hàng phải phù hợp với quy trình quản lý của cửa hàng; dữ liệu phát sinh từ giao dịch phải phục vụ được cả xử lý đơn hàng lẫn báo cáo quản trị.

Do đó, đề tài “Phát triển ứng dụng web quản lý mua bán sách” được lựa chọn vì đáp ứng đồng thời nhu cầu thực tiễn của hoạt động kinh doanh sách, nhu cầu nâng cao trải nghiệm của độc giả và yêu cầu vận dụng kiến thức chuyên ngành trong một sản phẩm phần mềm cụ thể. Hệ thống có lõi nghiệp vụ rõ ràng, có hướng mở rộng phù hợp và có khả năng tiếp tục hoàn thiện thành một nền tảng quản lý, mua bán và kết nối cộng đồng yêu sách.

### 1.1.2. Mục tiêu đề tài

Mục tiêu tổng quát của đề tài là phát triển ứng dụng web quản lý mua bán sách có khả năng hỗ trợ đồng bộ hoạt động của khách hàng và người quản trị. Hệ thống hướng đến số hóa quy trình từ khi người dùng tìm hiểu sách, lựa chọn và thực hiện giao dịch cho đến khi cửa hàng quản lý hàng hóa, xử lý đơn và theo dõi tình hình kinh doanh. Trên nền tảng đó, đề tài tiếp tục định hướng xây dựng các tiện ích giúp hoạt động mua bán sách gắn với trải nghiệm đọc, trao đổi và tái sử dụng sách.

Mục tiêu thứ nhất là xây dựng môi trường tra cứu và lựa chọn sách thuận tiện cho người dùng. Hệ thống cần cung cấp danh mục sách với những thông tin cần thiết để người đọc đưa ra quyết định, đồng thời hỗ trợ tìm kiếm và lọc theo nhu cầu. Mục tiêu này hướng đến việc giảm khó khăn khi người dùng phải tìm kiếm giữa nhiều đầu sách, đồng thời tạo điểm bắt đầu rõ ràng cho các bước tiếp theo như xem chi tiết, thêm vào giỏ hàng hoặc tham khảo đánh giá. Việc tổ chức thông tin sách nhất quán cũng là cơ sở để cửa hàng quản lý được danh mục sản phẩm của mình.

Mục tiêu thứ hai là hình thành quy trình mua sách trực tuyến hoàn chỉnh. Khách hàng cần có khả năng đăng ký và đăng nhập tài khoản, lựa chọn sách, điều chỉnh số lượng trong giỏ hàng, cung cấp thông tin nhận hàng và tạo đơn mua. Kết quả của quy trình là đơn hàng được ghi nhận với các thông tin cần thiết để người mua theo dõi và người bán xử lý. Mục tiêu này không chỉ nhằm đưa các thao tác mua sách lên môi trường web mà còn bảo đảm các bước có sự liên kết: sách được chọn phải được phản ánh trong giỏ hàng; giỏ hàng phải là cơ sở để hình thành đơn; và thông tin đơn phải hỗ trợ được việc quản lý sau giao dịch.

Mục tiêu thứ ba là giảm rào cản trong khâu thanh toán và xác nhận giao dịch. Hệ thống định hướng tích hợp thanh toán VietQR thông qua PayOS, cho phép khách hàng thanh toán bằng mã QR thay cho việc tự nhập nhiều thông tin chuyển khoản. Sau khi giao dịch được xác nhận, hệ thống cần phản ánh trạng thái thanh toán của đơn hàng, cập nhật dữ liệu liên quan và gửi thông báo phù hợp. Mục tiêu của thành phần này là giúp quy trình thanh toán minh bạch hơn cho khách hàng, đồng thời giảm công việc đối soát thủ công cho cửa hàng. Việc gửi thư điện tử xác nhận giao dịch cũng được định hướng để người mua có thêm căn cứ lưu trữ và tra cứu thông tin đơn hàng.

Mục tiêu thứ tư là nâng cao chất lượng hỗ trợ người đọc thông qua trí tuệ nhân tạo. Hệ thống dự kiến có trợ lý AI tiếp nhận các câu hỏi hoặc mô tả nhu cầu bằng ngôn ngữ tự nhiên, từ đó hỗ trợ gợi ý sách theo sở thích, cảm xúc hoặc mục tiêu đọc. Một hướng chức năng khác là cung cấp bản tóm tắt ngắn giúp người dùng nắm được những ý chính trước khi quyết định đọc hoặc mua sách. Mục tiêu này xuất phát từ nhận định rằng việc tìm sách không phải lúc nào cũng có thể diễn đạt bằng tên sách hay danh mục cố định. AI được định hướng là thành phần hỗ trợ ra quyết định cho người đọc, không thay thế quyền lựa chọn của người dùng.

Mục tiêu thứ năm là phát triển cơ chế ký gửi và tái lưu thông sách đã qua sử dụng. Theo định hướng của đề tài, khách hàng có thể đăng thông tin sách cũ cần ký gửi; quản trị viên thực hiện kiểm duyệt trước khi sách được đưa đến người mua phù hợp. Giá trị của sách ký gửi có thể được quy đổi thành Book Coins để người dùng tiếp tục sử dụng trong hệ thống. Mục tiêu này nhằm mở rộng mô hình bán sách mới sang mô hình tuần hoàn, tạo thêm lựa chọn tiếp cận sách cho người đọc và góp phần hạn chế tình trạng sách còn giá trị sử dụng nhưng bị bỏ quên sau khi đọc.

Mục tiêu thứ sáu là xây dựng trải nghiệm cá nhân gắn với thói quen đọc. Kệ sách ảo được định hướng là nơi người dùng quản lý các sách quan tâm hoặc đang đọc; thử thách đọc hỗ trợ việc xác lập và theo dõi mục tiêu cá nhân; chức năng đánh giá giúp người đọc chia sẻ nhận xét về sách. Các thành phần này được xây dựng nhằm tăng tương tác giữa người dùng với nội dung sách và cộng đồng đọc, đồng thời bổ sung thông tin tham khảo cho những người dùng khác. Mục tiêu không chỉ là tăng số lượng chức năng, mà là kết nối việc mua sách với quá trình sử dụng sách sau khi mua.

Mục tiêu thứ bảy là hỗ trợ quản trị viên vận hành cửa hàng dựa trên thông tin tập trung. Hệ thống cần hỗ trợ quản lý danh mục và tình trạng sách, xử lý đơn hàng theo vòng đời phù hợp, kiểm duyệt sách ký gửi và theo dõi các thông tin phát sinh cần chú ý. Bảng điều khiển, thông báo và báo cáo cần giúp quản trị viên theo dõi doanh thu, số đơn hàng, số lượng sách theo chi tiết đơn, số khách hàng, số đầu sách và lượng tồn; đồng thời quan sát xu hướng doanh thu, sách bán chạy, phân bố trạng thái đơn, đơn hàng gần đây, tồn kho thấp và hàng tồn luân chuyển chậm. Nhờ đó, dữ liệu phát sinh từ hoạt động của khách hàng có thể được chuyển thành thông tin hỗ trợ quản lý thay vì chỉ được lưu trữ rời rạc.

Mục tiêu thứ tám là bảo đảm hệ thống được phát triển theo các yêu cầu cơ bản về phân quyền, bảo vệ giao dịch và khả năng mở rộng. Người dùng cần được tiếp cận đúng các chức năng phù hợp với vai trò của mình; các thao tác ảnh hưởng đến đơn hàng và hàng hóa cần được xử lý nhất quán; các dịch vụ bên ngoài như cổng thanh toán, thư điện tử và AI cần được tích hợp theo cơ chế phù hợp. Đây là mục tiêu nền tảng để các chức năng phục vụ khách hàng và quản trị có thể hoạt động ổn định trong cùng một ứng dụng.

Về mặt học tập và nghiên cứu, đề tài hướng đến việc vận dụng kiến thức phân tích, thiết kế và phát triển phần mềm vào một bài toán cụ thể. Quá trình thực hiện là cơ hội để làm rõ mối liên hệ giữa yêu cầu nghiệp vụ, đối tượng sử dụng, dữ liệu quản lý và giải pháp hệ thống. Kết quả mong đợi là một ứng dụng web có định hướng triển khai thực tế, đồng thời là cơ sở để nhóm tiếp tục hoàn thiện các chức năng mở rộng trong những giai đoạn tiếp theo.

## 1.2. Đối tượng và phạm vi nghiên cứu

Đối tượng và phạm vi nghiên cứu được xác định nhằm làm rõ những thành phần, nghiệp vụ và vấn đề mà đề tài tập trung giải quyết. Đối tượng nghiên cứu trả lời cho câu hỏi hệ thống cần xem xét những ai, những dữ liệu nào và những hoạt động nào; phạm vi nghiên cứu xác định ranh giới thực hiện của đề tài về nghiệp vụ, môi trường sử dụng và mức độ phát triển chức năng. Việc phân định này giúp báo cáo giữ được tính tập trung, đồng thời tạo cơ sở cho các bước phân tích yêu cầu và thiết kế ở chương tiếp theo.

Với đề tài phát triển ứng dụng web quản lý mua bán sách, đối tượng nghiên cứu không chỉ là việc đưa danh mục sách lên môi trường trực tuyến. Đề tài xem xét một chuỗi tương tác bao gồm nhu cầu tìm và mua sách của người đọc, hoạt động quản lý của cửa hàng, dữ liệu giao dịch hình thành trong quá trình mua bán và các dịch vụ hỗ trợ như thanh toán, thư điện tử và AI. Các đối tượng này có quan hệ phụ thuộc: dữ liệu sách là cơ sở cho việc tìm kiếm và đặt mua; đơn hàng hình thành từ lựa chọn của khách hàng; dữ liệu đơn hàng lại hỗ trợ quản trị viên theo dõi hoạt động kinh doanh.

### 1.2.1. Đối tượng nghiên cứu

Đối tượng nghiên cứu của đề tài được phân thành năm nhóm chính, thể hiện trong Bảng 1.1.

**Bảng 1.1. Các nhóm đối tượng nghiên cứu của đề tài**

| Nhóm đối tượng | Đối tượng cụ thể | Nội dung nghiên cứu | Mối liên hệ trong hệ thống |
|---|---|---|---|
| Người sử dụng | Khách vãng lai, khách hàng, quản trị viên/người bán | Nhu cầu truy cập, tìm sách, mua sách, quản lý tài khoản, xử lý đơn hàng và quản trị vận hành | Mỗi nhóm người dùng có quyền truy cập và mục đích sử dụng khác nhau; các yêu cầu này là căn cứ xác định chức năng và phân quyền. |
| Nghiệp vụ mua bán sách | Danh mục, tìm kiếm, giỏ hàng, đặt hàng, thanh toán, theo dõi đơn hàng | Quy trình chuyển đổi từ nhu cầu mua của khách hàng thành giao dịch được ghi nhận và xử lý | Là luồng nghiệp vụ cốt lõi, kết nối dữ liệu sách, tài khoản, giỏ hàng và đơn hàng. |
| Nghiệp vụ quản lý và vận hành | Quản lý sách, tồn kho, đơn hàng, sách ký gửi, thông báo và báo cáo | Cách cửa hàng tiếp nhận, cập nhật, kiểm soát và khai thác thông tin phát sinh | Sử dụng dữ liệu từ hoạt động mua bán để hỗ trợ điều hành và duy trì tính sẵn sàng của hàng hóa. |
| Dữ liệu nghiệp vụ | Thông tin người dùng, sách, giỏ hàng, đơn hàng, chi tiết đơn, đánh giá, sách ký gửi, Book Coins, kệ sách ảo | Các thông tin cần được tổ chức, lưu trữ và liên kết để đáp ứng chức năng hệ thống | Dữ liệu là cầu nối giữa thao tác của người dùng, xử lý nghiệp vụ và thông tin hiển thị cho quản trị viên. |
| Dịch vụ và thành phần hỗ trợ | PayOS, dịch vụ thư điện tử, AI Gateway | Vai trò của các dịch vụ ngoài trong thanh toán, gửi thông báo giao dịch và hỗ trợ tư vấn/tóm tắt sách | Các thành phần này mở rộng khả năng của hệ thống nhưng cần được liên kết với nghiệp vụ và dữ liệu nội bộ. |

Thứ nhất, nhóm người sử dụng là đối tượng nghiên cứu trực tiếp vì chất lượng của hệ thống được đánh giá qua khả năng đáp ứng nhu cầu của các nhóm này. Khách vãng lai cần tiếp cận thông tin cơ bản về sách và có thể được giới hạn ở một số chức năng tư vấn. Khách hàng là người đã có tài khoản, sử dụng các chức năng phục vụ mua sách, quản lý giỏ hàng, theo dõi đơn, đánh giá, ký gửi và quản lý không gian đọc cá nhân. Quản trị viên hoặc người bán chịu trách nhiệm vận hành hệ thống ở góc độ cửa hàng, bao gồm quản lý sách, xử lý đơn, kiểm duyệt sách ký gửi và xem thông tin báo cáo. Sự khác nhau về mục đích sử dụng là cơ sở để nghiên cứu phân quyền và thiết kế luồng tương tác phù hợp.

Thứ hai, đề tài nghiên cứu quy trình mua bán sách trực tuyến như một chuỗi nghiệp vụ liên tục. Quá trình này bắt đầu từ việc người dùng tiếp cận và lựa chọn sách, tiếp tục qua giỏ hàng, thông tin đặt mua và thanh toán, rồi kết thúc ở việc đơn hàng được xác nhận, theo dõi hoặc hoàn tất. Mỗi giai đoạn cần làm rõ dữ liệu đầu vào, kết quả đầu ra và trách nhiệm xử lý để tránh đứt đoạn giữa trải nghiệm khách hàng với hoạt động vận hành của cửa hàng. Đối tượng nghiên cứu này là trọng tâm của việc phân tích use case, mô tả quy trình và thiết kế dữ liệu ở các chương sau.

Thứ ba, hoạt động quản lý của cửa hàng là đối tượng nghiên cứu cần thiết để hệ thống không chỉ dừng ở phía người mua. Cửa hàng cần quản lý thông tin sách và số lượng hàng hóa; tiếp nhận và xử lý các đơn phát sinh; đồng thời theo dõi các thông tin có ý nghĩa điều hành. Khi phát triển mô hình ký gửi, hoạt động kiểm duyệt sách cũ và ghi nhận giá trị quy đổi cũng trở thành một phần của nghiệp vụ. Nghiên cứu nhóm đối tượng này bảo đảm giải pháp đề xuất cân bằng giữa tính thuận tiện cho khách hàng và khả năng kiểm soát của người quản trị.

Thứ tư, đề tài nghiên cứu dữ liệu nghiệp vụ và mối liên kết giữa các nhóm dữ liệu. Ví dụ, một đơn hàng cần gắn với khách hàng tạo đơn và các sách được đặt mua; đánh giá cần gắn với người dùng và cuốn sách tương ứng; sách ký gửi cần có thông tin người gửi, trạng thái kiểm duyệt và trạng thái giao dịch. Việc xác định đúng các thực thể dữ liệu từ đầu giúp hệ thống có cơ sở lưu vết hoạt động, tránh trùng lặp thông tin và hỗ trợ tra cứu khi cần thiết. Thiết kế chi tiết các bảng dữ liệu, thuộc tính và quan hệ sẽ được trình bày trong phần thiết kế cơ sở dữ liệu.

Thứ năm, đề tài nghiên cứu vai trò của các thành phần tích hợp trong việc hoàn thiện trải nghiệm và tự động hóa nghiệp vụ. Cổng thanh toán hỗ trợ tiếp nhận kết quả giao dịch; dịch vụ thư điện tử hỗ trợ gửi thông báo; AI Gateway hỗ trợ xử lý yêu cầu tư vấn hoặc tóm tắt nội dung. Các thành phần này không được xem là các hệ thống độc lập trong phạm vi đề tài, mà là dịch vụ hỗ trợ được kết nối với ứng dụng web để phục vụ mục tiêu mua bán, quản lý và hỗ trợ đọc sách. Cách nhìn này giúp xác định rõ ranh giới trách nhiệm giữa hệ thống cần phát triển và các dịch vụ bên ngoài.

Tóm lại, đối tượng nghiên cứu của đề tài gồm con người, nghiệp vụ, dữ liệu và các thành phần hỗ trợ tạo nên hệ thống quản lý mua bán sách. Việc nghiên cứu đồng thời các nhóm đối tượng này giúp bảo đảm giải pháp được xây dựng từ nhu cầu thực tế của người sử dụng, đồng thời có khả năng đáp ứng yêu cầu quản lý và mở rộng theo định hướng của đề tài.

### 1.2.2. Phạm vi nghiên cứu

Phạm vi nghiên cứu của đề tài được xác định theo hướng xây dựng một ứng dụng web phục vụ quản lý và kinh doanh sách trực tuyến. Hệ thống tập trung vào các hoạt động diễn ra từ khi người dùng tiếp cận sách đến khi giao dịch được xử lý, đồng thời bao quát những nghiệp vụ quản trị cần thiết để cửa hàng duy trì danh mục, hàng hóa và thông tin kinh doanh. Phạm vi này được lựa chọn để bảo đảm đề tài vừa giải quyết được nghiệp vụ cốt lõi của cửa hàng sách, vừa có khả năng mở rộng thành nền tảng hỗ trợ trải nghiệm đọc và trao đổi sách.

**Bảng 1.2. Phạm vi nghiên cứu của đề tài**

| Khía cạnh | Phạm vi nghiên cứu |
|---|---|
| Đối tượng phục vụ | Độc giả cá nhân, học sinh, sinh viên, người yêu sách; người bán hoặc quản trị viên vận hành cửa hàng. |
| Môi trường sử dụng | Ứng dụng web có thiết kế đáp ứng, định hướng sử dụng trên máy tính để bàn, máy tính bảng và điện thoại di động. |
| Mô hình kinh doanh | Bán sách mới trực tiếp cho khách hàng theo mô hình B2C; ký gửi sách đã qua sử dụng theo mô hình C2B2C, trong đó sách được kiểm duyệt trước khi bán lại. |
| Phạm vi nghiệp vụ khách hàng | Đăng ký, đăng nhập, tìm kiếm và xem sách; quản lý giỏ hàng; đặt hàng, thanh toán VietQR, theo dõi đơn; đánh giá sách; sử dụng trợ lý AI; quản lý kệ sách ảo, thử thách đọc và yêu cầu ký gửi. |
| Phạm vi nghiệp vụ quản trị | Quản lý kho sách, xử lý đơn hàng, kiểm duyệt sách ký gửi, theo dõi thông báo và xem báo cáo thống kê về doanh thu, đơn hàng, khách hàng, sách bán ra và tình trạng hàng hóa. |
| Dịch vụ hỗ trợ | Thanh toán qua PayOS; gửi thư điện tử giao dịch qua máy chủ thư; tích hợp AI Gateway cho tư vấn và tóm tắt sách. |

Về phạm vi người dùng, hệ thống ưu tiên phục vụ những người có nhu cầu tiếp cận, mua hoặc trao đổi sách trên môi trường trực tuyến. Độc giả có thể là người mới truy cập để tham khảo danh mục, người đã có tài khoản để mua sách và sử dụng các tiện ích cá nhân, hoặc người muốn ký gửi sách cũ. Bên cạnh đó, người bán/quản trị viên là nhóm sử dụng hệ thống để thực hiện các công việc vận hành. Việc xác định đồng thời hai nhóm chính này giúp đề tài tránh xem hoạt động mua sách như một giao dịch đơn lẻ, mà đặt nó trong mối quan hệ với quá trình tiếp nhận, kiểm soát và xử lý của cửa hàng.

Về phạm vi nghiệp vụ bán sách mới, đề tài xem xét chuỗi hoạt động từ tìm kiếm, xem thông tin sách, chọn sách, quản lý giỏ hàng, đặt hàng, thanh toán đến theo dõi trạng thái đơn. Thanh toán VietQR được đưa vào phạm vi nghiên cứu nhằm hỗ trợ xác nhận giao dịch theo quy trình điện tử. Sau khi thanh toán được xác nhận, thông tin đơn hàng và tình trạng hàng hóa là dữ liệu cần được cập nhật để phục vụ bước xử lý tiếp theo. Các yêu cầu chi tiết về luồng đơn hàng, điều kiện xử lý và dữ liệu trao đổi sẽ được đặc tả trong các phần phân tích use case và thiết kế hệ thống.

Về phạm vi mô hình tuần hoàn, đề tài nghiên cứu nghiệp vụ ký gửi sách cũ giữa người dùng và cửa hàng. Người dùng cung cấp thông tin sách cần ký gửi; quản trị viên đóng vai trò kiểm duyệt trước khi sách được công bố cho người mua khác. Giá trị phát sinh từ sách ký gửi được định hướng quy đổi thành Book Coins để khuyến khích người dùng tiếp tục tham gia hệ thống. Phạm vi này giúp đề tài mở rộng từ việc bán sách mới sang tạo điều kiện để sách đã qua sử dụng tiếp tục được khai thác, phù hợp với định hướng xây dựng hệ sinh thái sách tuần hoàn.

Về phạm vi hỗ trợ trải nghiệm đọc, đề tài bao gồm các chức năng tư vấn và tương tác xoay quanh sách. Trợ lý AI được định hướng hỗ trợ tìm kiếm sách theo nhu cầu hoặc cảm xúc và cung cấp nội dung tóm tắt ngắn. Người dùng có thể đánh giá sách, quản lý kệ sách ảo và theo dõi thử thách đọc. Những chức năng này được nghiên cứu với vai trò bổ trợ cho quá trình tìm hiểu và sử dụng sách; chúng không tách rời khỏi nghiệp vụ mua bán mà góp phần giúp người dùng tiếp cận thông tin phù hợp hơn và duy trì động lực đọc.

Về phạm vi quản trị, đề tài bao gồm việc quản lý thông tin sách và số lượng hàng hóa, theo dõi vòng đời đơn hàng, kiểm duyệt nội dung ký gửi, tiếp nhận thông báo và khai thác số liệu thống kê. Phạm vi thống kê bao gồm doanh thu, số đơn hàng, số lượng sách theo chi tiết đơn, số khách hàng, số đầu sách và lượng tồn; đồng thời hỗ trợ theo dõi xu hướng doanh thu, sách bán chạy, phân bố trạng thái đơn, đơn hàng gần đây, đầu sách tồn thấp và hàng tồn luân chuyển chậm. Các chỉ tiêu này giúp người vận hành quan sát cả kết quả kinh doanh lẫn những vấn đề cần xử lý trong kho hàng.

Về phạm vi kỹ thuật và môi trường hoạt động, đề tài tập trung vào ứng dụng web đa nền tảng với giao diện đáp ứng trên các kích thước màn hình phổ biến. Hệ thống có kết nối với các dịch vụ bên ngoài phục vụ thanh toán, gửi thư điện tử và AI; tuy nhiên, các dịch vụ này được tiếp cận như thành phần tích hợp để thực hiện yêu cầu nghiệp vụ của ứng dụng. Phần công nghệ, kiến trúc triển khai, mô hình dữ liệu và cơ chế tích hợp sẽ được trình bày chi tiết ở các chương sau.

Như vậy, phạm vi nghiên cứu bao quát các thành phần cần thiết để hình thành hệ thống quản lý mua bán sách trực tuyến theo định hướng thông minh và tuần hoàn. Việc giới hạn phạm vi ở các nghiệp vụ, đối tượng và môi trường nêu trên giúp đề tài có định hướng triển khai rõ ràng, đồng thời bảo đảm các nội dung phân tích và thiết kế tập trung vào mục tiêu đã xác định.

## 1.3. Dự kiến công việc và phân công nhiệm vụ

Đề tài được thực hiện bởi nhóm gồm bốn thành viên. Việc phân công được xây dựng theo các nhóm công việc có quan hệ với nhau, bao gồm khảo sát và phân tích yêu cầu, thiết kế giải pháp, phát triển các chức năng, kiểm thử và hoàn thiện báo cáo. Mỗi thành viên phụ trách một nhóm nội dung chính để bảo đảm tiến độ và trách nhiệm cá nhân; đồng thời, các thành viên phối hợp trong các phần có tính liên thông như thống nhất yêu cầu, kiểm thử tích hợp và rà soát báo cáo.

Việc phân công không tách rời hoàn toàn theo từng màn hình hoặc từng tệp mã nguồn, bởi một chức năng có thể liên quan đến giao diện, xử lý nghiệp vụ và dữ liệu. Do đó, thành viên phụ trách chính có trách nhiệm điều phối phần việc được giao, tổng hợp kết quả và phối hợp với các thành viên khác khi cần kết nối chức năng. Bảng 1.3 trình bày dự kiến phân công nhiệm vụ của nhóm.

**Bảng 1.3. Bảng phân công nhiệm vụ thực hiện**

| STT | Nội dung công việc | Người thực hiện | Vai trò |
|---|---|---|---|
| 1 | Khảo sát bài toán; tổng hợp yêu cầu; xác định tác nhân, yêu cầu chức năng, yêu cầu phi chức năng và use case; xây dựng các nội dung tổng quan của báo cáo. Phối hợp rà soát tính nhất quán giữa các phần của đề tài. | [CẦN BỔ SUNG: Họ và tên thành viên 1] | Nhóm trưởng |
| 2 | Phân tích và phát triển các chức năng phía khách hàng: tài khoản, danh mục sách, tìm kiếm, xem thông tin sách, giỏ hàng, đánh giá và các giao diện liên quan. Phối hợp thiết kế trải nghiệm người dùng. | [CẦN BỔ SUNG: Họ và tên thành viên 2] | Thành viên |
| 3 | Phân tích và phát triển nghiệp vụ đơn hàng, thanh toán VietQR, theo dõi trạng thái đơn, gửi thư điện tử và các luồng tích hợp với dịch vụ bên ngoài. Phối hợp kiểm thử các giao dịch liên quan. | [CẦN BỔ SUNG: Họ và tên thành viên 3] | Thành viên |
| 4 | Phân tích và phát triển các chức năng quản trị, quản lý kho, thống kê, ký gửi sách cũ, Book Coins, kệ sách ảo và định hướng tích hợp AI. Phụ trách hỗ trợ thiết kế dữ liệu, kiểm thử tổng thể và hoàn thiện tài liệu kỹ thuật. | [CẦN BỔ SUNG: Họ và tên thành viên 4] | Thành viên |

Nhóm trưởng chịu trách nhiệm điều phối tiến độ, tổng hợp yêu cầu và rà soát sự thống nhất của sản phẩm, mã nguồn và báo cáo. Các thành viên còn lại phụ trách những nhóm nghiệp vụ có tính chuyên biệt, nhưng cùng tham gia thảo luận yêu cầu, kiểm thử liên chức năng và hoàn thiện báo cáo. Cách phân công này giúp bảo đảm các phần việc được phân chia rõ ràng, đồng thời hạn chế tình trạng một chức năng bị tách rời khỏi các thành phần có liên quan.

Thông tin họ tên, mã sinh viên, lớp hành chính và mức đóng góp thực tế của từng thành viên cần được nhóm cập nhật trước khi nộp báo cáo để bảo đảm tính chính xác của bảng phân công.

## 1.4. Xác định tác nhân, chức năng, phi chức năng của hệ thống

Xác định tác nhân là bước đầu để phân tích yêu cầu hệ thống. Mỗi tác nhân đại diện cho một cá nhân, một nhóm người dùng hoặc một hệ thống bên ngoài có tương tác với ứng dụng. Việc làm rõ tác nhân giúp xác định đúng nhu cầu sử dụng, phạm vi quyền hạn và các luồng trao đổi dữ liệu của hệ thống. Trên cơ sở các tác nhân được xác định, các yêu cầu chức năng và phi chức năng sẽ được trình bày tại các mục tiếp theo.

Trong hệ thống quản lý mua bán sách, tác nhân không chỉ bao gồm người trực tiếp sử dụng giao diện web mà còn bao gồm các dịch vụ ngoài tham gia vào quy trình thanh toán, xử lý AI và gửi thư điện tử. Các dịch vụ này không thao tác như người dùng thông thường, nhưng cung cấp hoặc tiếp nhận dữ liệu cần thiết để hoàn thành một số nghiệp vụ của hệ thống.

### 1.4.1. Xác định tác nhân hệ thống

Các tác nhân của hệ thống được trình bày trong Bảng 1.4.

**Bảng 1.4. Danh sách tác nhân trong hệ thống**

<table>
  <tr>
    <th>Tác nhân</th>
    <th>Mô tả vai trò</th>
    <th>Các chức năng liên quan</th>
  </tr>
  <tr>
    <td>Khách vãng lai<br>(Guest)</td>
    <td>- Người truy cập ứng dụng nhưng chưa đăng nhập tài khoản.<br>- Tiếp cận các nội dung công khai trước khi trở thành khách hàng của hệ thống.</td>
    <td>Xem danh mục và thông tin sách<br>Sử dụng ở mức giới hạn chức năng hỗ trợ tư vấn<br>Đăng ký tài khoản<br>Đăng nhập hệ thống</td>
  </tr>
  <tr>
    <td>Khách hàng<br>(Customer)</td>
    <td>- Người dùng đã đăng ký và đăng nhập với vai trò khách hàng.<br>- Là tác nhân trung tâm của hoạt động mua sách, tương tác nội dung sách và sử dụng các tiện ích cá nhân.</td>
    <td>Tìm kiếm, xem và lựa chọn sách<br>Quản lý giỏ hàng<br>Đặt hàng và thanh toán<br>Theo dõi hoặc hủy đơn ở trạng thái phù hợp<br>Đánh giá sách<br>Sử dụng trợ lý AI<br>Quản lý kệ sách ảo và thử thách đọc<br>Gửi yêu cầu ký gửi sách cũ</td>
  </tr>
  <tr>
    <td>Quản trị viên/<br>Người bán<br>(Seller/Admin)</td>
    <td>- Người vận hành hoạt động kinh doanh sách.<br>- Kiểm soát các nghiệp vụ quản trị trên hệ thống.</td>
    <td>Quản lý kho sách<br>Xử lý và cập nhật trạng thái đơn hàng<br>Kiểm duyệt sách ký gửi<br>Theo dõi thông báo<br>Xem báo cáo và thống kê phục vụ quản trị</td>
  </tr>
  <tr>
    <td>Hệ thống thanh toán<br>PayOS</td>
    <td>- Dịch vụ thanh toán bên ngoài được tích hợp để hỗ trợ giao dịch trực tuyến bằng VietQR.</td>
    <td>Tiếp nhận yêu cầu tạo thanh toán<br>Cung cấp thông tin thanh toán VietQR<br>Gửi thông báo xác nhận giao dịch về hệ thống</td>
  </tr>
  <tr>
    <td>AI Gateway</td>
    <td>- Dịch vụ AI bên ngoài xử lý truy vấn ngôn ngữ tự nhiên phục vụ người dùng.</td>
    <td>Tiếp nhận yêu cầu tư vấn hoặc tóm tắt sách<br>Xử lý nội dung<br>Trả kết quả để ứng dụng hiển thị cho người dùng</td>
  </tr>
  <tr>
    <td>Mail Server</td>
    <td>- Máy chủ hoặc dịch vụ thư điện tử dùng để gửi thư giao dịch.</td>
    <td>Tiếp nhận yêu cầu gửi thư<br>Chuyển thư xác nhận đơn hàng hoặc thông báo giao dịch tới người nhận</td>
  </tr>
</table>

Khách vãng lai và khách hàng là hai tác nhân đại diện cho phía người đọc nhưng có phạm vi tương tác khác nhau. Khách vãng lai chủ yếu tiếp cận nội dung công khai và được khuyến khích tạo tài khoản khi cần thực hiện giao dịch hoặc sử dụng các chức năng cá nhân. Khách hàng là tác nhân trung tâm của quy trình mua bán vì các thao tác lựa chọn sách, đặt hàng, thanh toán, đánh giá và ký gửi đều được thực hiện dưới danh tính tài khoản. Việc phân biệt hai tác nhân này là cần thiết để bảo vệ dữ liệu cá nhân và giới hạn các chức năng chỉ dành cho người dùng đã được xác thực.

Quản trị viên/người bán là tác nhân đại diện cho phía vận hành cửa hàng. Khác với khách hàng, tác nhân này không tập trung vào việc mua sách mà cần kiểm soát dữ liệu và quá trình xử lý nghiệp vụ. Các thao tác của quản trị viên tác động đến khả năng cung ứng sách, tiến độ xử lý đơn hàng và chất lượng nội dung được công bố trên hệ thống, đặc biệt đối với sách ký gửi. Vì vậy, quyền truy cập của tác nhân này cần được phân tách rõ với quyền của khách hàng.

Ba tác nhân còn lại là các hệ thống bên ngoài. PayOS tham gia vào quy trình thanh toán và xác nhận giao dịch; AI Gateway hỗ trợ các yêu cầu tư vấn, tìm kiếm theo nhu cầu và tóm tắt; Mail Server hỗ trợ chuyển thông tin giao dịch qua thư điện tử. Việc xác định các tác nhân ngoài hệ thống giúp mô tả đầy đủ ranh giới tương tác của ứng dụng, đồng thời là cơ sở để xây dựng các biểu đồ use case và biểu đồ trình tự ở chương phân tích, thiết kế.

### 1.4.2. Yêu cầu chức năng

Yêu cầu chức năng mô tả các nghiệp vụ mà hệ thống cần cung cấp để đáp ứng nhu cầu của khách hàng, quản trị viên và các quy trình tích hợp. Các yêu cầu được xác định từ mục tiêu, phạm vi và tác nhân của hệ thống; mỗi yêu cầu được gán một mã để thuận tiện cho việc tham chiếu khi phân tích use case, thiết kế và kiểm thử.

**Bảng 1.5. Danh sách yêu cầu chức năng của hệ thống**

<table>
  <tr>
    <th>Mã</th>
    <th>Tên</th>
    <th>Mô tả</th>
  </tr>
  <tr><td>FR01</td><td>Đăng ký tài khoản</td><td>- Cho phép khách vãng lai tạo tài khoản khách hàng.<br>- Kiểm tra tính hợp lệ dữ liệu và tránh tạo tài khoản trùng lặp.</td></tr>
  <tr><td>FR02</td><td>Đăng nhập và đăng xuất</td><td>- Cho phép người dùng xác thực để truy cập chức năng theo vai trò.<br>- Thiết lập phiên sau khi đăng nhập và kết thúc phiên khi đăng xuất.</td></tr>
  <tr><td>FR03</td><td>Tìm kiếm, lọc và xem danh mục sách</td><td>- Cho phép xem danh mục sách.<br>- Tìm kiếm theo thông tin sách và lọc theo các tiêu chí được cung cấp.<br>- Hiển thị kết quả theo danh sách có phân trang.</td></tr>
  <tr><td>FR04</td><td>Xem chi tiết và tóm tắt sách</td><td>- Hiển thị nội dung mô tả, giá, số lượng và thông tin đánh giá của đầu sách.<br>- Hỗ trợ yêu cầu tóm tắt ngắn bằng AI.</td></tr>
  <tr><td>FR05</td><td>Đánh giá và nhận xét sách</td><td>- Cho phép khách hàng đã đăng nhập gửi đánh giá theo thang điểm sao và nội dung nhận xét.<br>- Lưu và hiển thị đánh giá liên quan đến đầu sách.</td></tr>
  <tr><td>FR06</td><td>Quản lý giỏ hàng</td><td>- Thêm sách vào giỏ, điều chỉnh số lượng hoặc xóa sách khỏi giỏ.<br>- Kiểm tra số lượng lựa chọn phù hợp với số lượng có thể cung cấp.</td></tr>
  <tr><td>FR07</td><td>Đặt hàng và thanh toán VietQR</td><td>- Tạo đơn hàng từ giỏ hàng và tiếp nhận thông tin giao nhận.<br>- Thanh toán bằng mã VietQR thông qua PayOS.<br>- Ghi nhận đơn hàng và tiếp nhận kết quả thanh toán.</td></tr>
  <tr><td>FR08</td><td>Xác nhận thanh toán và gửi thông báo giao dịch</td><td>- Tiếp nhận thông báo xác nhận từ cổng thanh toán.<br>- Cập nhật trạng thái đơn hàng và số lượng sách liên quan.<br>- Gửi thư điện tử xác nhận giao dịch.</td></tr>
  <tr><td>FR09</td><td>Theo dõi và quản lý đơn hàng của khách hàng</td><td>- Xem lịch sử đơn hàng và tra cứu trạng thái xử lý.<br>- Gửi yêu cầu hủy đơn khi đơn ở trạng thái cho phép hủy.</td></tr>
  <tr><td>FR10</td><td>Trợ lý AI tư vấn sách</td><td>- Nhận câu hỏi hoặc mô tả nhu cầu đọc bằng ngôn ngữ tự nhiên.<br>- Chuyển yêu cầu đến AI Gateway và hiển thị gợi ý sách hoặc phản hồi hỗ trợ.</td></tr>
  <tr><td>FR11</td><td>Ký gửi sách cũ và quản lý Book Coins</td><td>- Gửi yêu cầu ký gửi sách đã qua sử dụng.<br>- Quản lý trạng thái ký gửi, kiểm duyệt và bán lại sách.<br>- Ghi nhận Book Coins theo quy tắc của hệ thống.</td></tr>
  <tr><td>FR12</td><td>Quản lý kệ sách ảo và thử thách đọc</td><td>- Quản lý danh sách sách cá nhân.<br>- Theo dõi tiến độ đọc và thiết lập hoặc theo dõi mục tiêu đọc.</td></tr>
  <tr><td>FR13</td><td>Quản lý kho sách và đơn hàng</td><td>- Thêm, cập nhật hoặc ngừng cung cấp sách.<br>- Theo dõi đơn hàng và cập nhật trạng thái xử lý theo quyền quản trị.</td></tr>
  <tr><td>FR14</td><td>Quản trị sách ký gửi và thông báo</td><td>- Xem, phê duyệt hoặc từ chối yêu cầu ký gửi.<br>- Tiếp nhận và quản lý thông báo về hoạt động cần xử lý.</td></tr>
  <tr><td>FR15</td><td>Báo cáo và thống kê kinh doanh</td><td>- Xem doanh thu, số đơn, số khách hàng, số đầu sách và lượng tồn.<br>- Xem biểu đồ doanh thu theo thời gian và lọc theo mốc thời gian.<br>- Theo dõi sách bán chạy, trạng thái đơn, đơn gần đây, tồn kho thấp và hàng tồn luân chuyển chậm.</td></tr>
</table>

Nhóm yêu cầu từ FR01 đến FR09 tập trung vào quy trình cơ bản mà khách hàng cần thực hiện để mua sách trực tuyến. Các yêu cầu này liên kết thành một luồng xuyên suốt từ xác thực tài khoản, tiếp cận danh mục, quản lý giỏ hàng, tạo đơn, thanh toán đến theo dõi đơn. Việc tách các yêu cầu theo từng chức năng giúp xác định rõ trách nhiệm của hệ thống tại mỗi giai đoạn, đồng thời tạo cơ sở xây dựng các use case chi tiết.

FR10, FR11 và FR12 là các yêu cầu mở rộng theo định hướng xây dựng hệ thống thông minh và hệ sinh thái sách tuần hoàn. Trợ lý AI hỗ trợ người dùng tiếp cận sách từ nhu cầu thực tế; ký gửi và Book Coins tạo cơ chế để sách đã qua sử dụng tiếp tục được lưu thông; kệ sách ảo và thử thách đọc duy trì sự gắn kết của người dùng sau khi mua sách. Các yêu cầu này cần được liên kết với dữ liệu người dùng và dữ liệu sách để tạo trải nghiệm cá nhân hóa, nhất quán.

FR13, FR14 và FR15 thuộc nhóm yêu cầu quản trị. Các chức năng này bảo đảm cửa hàng có khả năng kiểm soát hàng hóa, xử lý giao dịch, quản lý nội dung ký gửi và khai thác thông tin tổng hợp. Sự phối hợp giữa nhóm chức năng khách hàng và nhóm chức năng quản trị là điều kiện để hệ thống vận hành trọn vẹn: thao tác của khách hàng tạo ra dữ liệu nghiệp vụ, còn thao tác quản trị sử dụng dữ liệu đó để duy trì và điều hành hoạt động kinh doanh.

### 1.4.3. Yêu cầu phi chức năng

Yêu cầu phi chức năng xác định các đặc tính chất lượng mà hệ thống cần đáp ứng trong quá trình vận hành. Nếu yêu cầu chức năng trả lời cho câu hỏi hệ thống thực hiện được việc gì, thì yêu cầu phi chức năng làm rõ hệ thống cần hoạt động như thế nào để bảo đảm tính an toàn, ổn định, thuận tiện và có khả năng phát triển. Các yêu cầu này đặc biệt quan trọng đối với ứng dụng có xử lý dữ liệu người dùng, giao dịch thanh toán và tích hợp dịch vụ bên ngoài.

**Bảng 1.6. Danh sách yêu cầu phi chức năng của hệ thống**

| Yêu cầu | Mô tả |
|---|---|
| Hiệu năng | Hệ thống cần xử lý các thao tác thông thường như xem danh mục, tìm kiếm, quản lý giỏ hàng và tra cứu đơn hàng trong thời gian phù hợp với trải nghiệm sử dụng web. Các tác vụ tích hợp hoặc gửi thư không nên làm gián đoạn việc phản hồi giao diện cho người dùng. |
| Bảo mật dữ liệu và truy vấn | Hệ thống cần bảo vệ dữ liệu người dùng và dữ liệu nghiệp vụ khỏi truy cập hoặc thay đổi trái phép. Các thao tác truy cập cơ sở dữ liệu cần sử dụng truy vấn có tham số để hạn chế nguy cơ chèn lệnh SQL. |
| Bảo mật thanh toán | Dữ liệu trao đổi trong quy trình thanh toán cần được xác thực trước khi được sử dụng để cập nhật đơn hàng. Thông báo giao dịch từ cổng thanh toán cần được kiểm tra tính hợp lệ nhằm hạn chế việc xác nhận sai hoặc giả mạo giao dịch. |
| Phân quyền và quản lý phiên | Hệ thống cần phân biệt quyền truy cập giữa khách vãng lai, khách hàng và quản trị viên/người bán. Những chức năng liên quan đến đơn hàng cá nhân, quản lý kho, báo cáo hoặc kiểm duyệt phải chỉ được truy cập bởi người dùng có quyền phù hợp. |
| Tính toàn vẹn dữ liệu | Các thao tác có nhiều thay đổi phụ thuộc lẫn nhau, đặc biệt là tạo đơn, xác nhận thanh toán và cập nhật số lượng sách, cần được xử lý nhất quán. Hệ thống cần hạn chế tình trạng đơn hàng được xác nhận nhưng dữ liệu chi tiết hoặc số lượng hàng hóa không được cập nhật tương ứng. |
| Độ tin cậy và khả năng chịu lỗi | Hệ thống cần có cơ chế xử lý ngoại lệ để tránh hiển thị thông tin kỹ thuật không cần thiết cho người dùng. Khi dịch vụ thanh toán gặp sự cố, hệ thống được định hướng có phương án dự phòng mã VietQR để giảm gián đoạn quá trình thanh toán. |
| Khả năng sử dụng | Giao diện cần rõ ràng, nhất quán và hỗ trợ người dùng thực hiện các thao tác chính như tìm sách, thêm vào giỏ, thanh toán và theo dõi đơn hàng. Thông báo thành công hoặc lỗi cần được diễn đạt dễ hiểu để người dùng biết trạng thái thao tác của mình. |
| Tương thích đa nền tảng | Ứng dụng cần có giao diện đáp ứng để sử dụng được trên máy tính để bàn, máy tính bảng và điện thoại di động. Nội dung và các thao tác chính cần được hiển thị phù hợp trên các kích thước màn hình phổ biến. |
| Khả năng bảo trì và mở rộng | Hệ thống cần được tổ chức theo cấu trúc có thể bảo trì, cho phép điều chỉnh hoặc bổ sung chức năng mà hạn chế ảnh hưởng đến các phần còn lại. Đây là điều kiện cần thiết để phát triển tiếp các hạng mục AI, ký gửi sách, Book Coins, kệ sách ảo và các tiện ích khác. |
| Khả năng tích hợp | Hệ thống cần có khả năng giao tiếp với các dịch vụ bên ngoài phục vụ thanh toán, gửi thư điện tử và AI. Việc tích hợp cần xác định rõ dữ liệu gửi đi, dữ liệu nhận về và cách xử lý khi dịch vụ ngoài không sẵn sàng. |

Các yêu cầu phi chức năng có mối quan hệ trực tiếp với yêu cầu chức năng đã xác định. Ví dụ, FR07 và FR08 về đặt hàng, thanh toán chỉ có thể tạo được niềm tin cho người dùng khi yêu cầu về bảo mật thanh toán, phân quyền và toàn vẹn dữ liệu được đáp ứng. Tương tự, FR10 về trợ lý AI hay FR11 về ký gửi sách cũ cần được xây dựng theo hướng có thể mở rộng và tích hợp với các thành phần ngoài hệ thống.

Những yêu cầu trong Bảng 1.6 là cơ sở để đánh giá giải pháp ở các chương sau. Chúng không chỉ định sẵn một kết quả đo lường khi chưa có dữ liệu kiểm thử, mà xác định các tiêu chí chất lượng cần được xem xét trong quá trình thiết kế, cài đặt và hoàn thiện hệ thống.

### 1.4.4. Xác định Use Case

Use case mô tả các mục tiêu mà tác nhân đạt được khi tương tác với hệ thống. Việc xác định use case giúp chuyển các yêu cầu chức năng thành những nhóm tương tác có thể phân tích, thiết kế và kiểm thử. Mỗi use case dưới đây có thể gồm một hoặc nhiều chức năng chi tiết; các use case có luồng xử lý phức tạp sẽ được đặc tả và mô hình hóa ở Chương II.

**Bảng 1.7. Danh sách Use Case của hệ thống**

| Use case | Use case chi tiết | Tác nhân liên quan (Actor) |
|---|---|---|
| UC01: Đăng ký và xác thực người dùng | UC01.1: Đăng ký tài khoản khách hàng.<br>UC01.2: Đăng nhập theo vai trò.<br>UC01.3: Đăng xuất và kết thúc phiên làm việc. | Khách vãng lai, Khách hàng, Quản trị viên/Người bán |
| UC02: Tìm kiếm và lọc sách | UC02.1: Xem danh mục sách.<br>UC02.2: Tìm kiếm theo từ khóa liên quan đến sách.<br>UC02.3: Lọc và phân trang danh sách sách. | Khách vãng lai, Khách hàng, Quản trị viên/Người bán |
| UC03: Xem chi tiết và tóm tắt sách | UC03.1: Xem thông tin chi tiết của sách.<br>UC03.2: Xem thông tin đánh giá liên quan.<br>UC03.3: Yêu cầu tóm tắt sách bằng AI. | Khách vãng lai, Khách hàng, AI Gateway |
| UC04: Đánh giá và viết nhận xét | UC04.1: Chọn mức đánh giá sao.<br>UC04.2: Viết và gửi nhận xét.<br>UC04.3: Hiển thị đánh giá của sách. | Khách hàng |
| UC05: Quản lý giỏ hàng | UC05.1: Thêm sách vào giỏ hàng.<br>UC05.2: Cập nhật số lượng sách.<br>UC05.3: Xóa sách khỏi giỏ hàng.<br>UC05.4: Đồng bộ và khôi phục giỏ hàng cá nhân. | Khách hàng |
| UC06: Đặt hàng và thanh toán VietQR | UC06.1: Cung cấp thông tin giao nhận.<br>UC06.2: Tạo đơn hàng từ giỏ hàng.<br>UC06.3: Tạo và hiển thị mã VietQR.<br>UC06.4: Xác nhận thanh toán và gửi thư giao dịch. | Khách hàng, Hệ thống thanh toán PayOS, Mail Server |
| UC07: Trợ lý AI tư vấn sách | UC07.1: Nhập câu hỏi hoặc mô tả nhu cầu đọc.<br>UC07.2: Phân tích nhu cầu hoặc cảm xúc.<br>UC07.3: Nhận và hiển thị gợi ý sách. | Khách vãng lai, Khách hàng, AI Gateway |
| UC08: Ký gửi và trao đổi sách cũ | UC08.1: Gửi yêu cầu ký gửi sách cũ.<br>UC08.2: Kiểm duyệt yêu cầu ký gửi.<br>UC08.3: Công bố sách được duyệt.<br>UC08.4: Ghi nhận Book Coins khi giao dịch hoàn tất. | Khách hàng, Quản trị viên/Người bán |
| UC09: Quản lý kệ sách ảo và thử thách đọc | UC09.1: Quản lý danh sách sách cá nhân.<br>UC09.2: Cập nhật tiến độ đọc.<br>UC09.3: Thiết lập và theo dõi thử thách đọc. | Khách hàng |
| UC10: Quản trị kho sách và cảnh báo tồn kho | UC10.1: Thêm sách mới.<br>UC10.2: Cập nhật thông tin, giá và số lượng sách.<br>UC10.3: Xóa hoặc ngừng cung cấp sách.<br>UC10.4: Theo dõi tình trạng tồn kho. | Quản trị viên/Người bán |
| UC11: Quản lý thông báo cho quản trị viên | UC11.1: Nhận thông báo về sự kiện cần xử lý.<br>UC11.2: Xem danh sách thông báo.<br>UC11.3: Đánh dấu thông báo đã đọc. | Quản trị viên/Người bán |
| UC12: Quản lý vòng đời đơn hàng và thống kê | UC12.1: Xem danh sách và chi tiết đơn hàng.<br>UC12.2: Cập nhật trạng thái xử lý đơn.<br>UC12.3: Hủy đơn theo quyền và điều kiện phù hợp.<br>UC12.4: Xem chỉ số tổng quan về doanh thu, đơn hàng, khách hàng và hàng hóa.<br>UC12.5: Lọc doanh thu theo mốc thời gian và xem xu hướng doanh thu.<br>UC12.6: Theo dõi sách bán chạy, trạng thái đơn, đơn gần đây, tồn kho thấp và hàng tồn luân chuyển chậm. | Khách hàng, Quản trị viên/Người bán |

Các use case từ UC01 đến UC06 tạo thành luồng hoạt động cơ bản của hệ thống mua bán sách: người dùng tạo hoặc sử dụng tài khoản, tiếp cận danh mục, lựa chọn sách, quản lý giỏ hàng và hoàn tất giao dịch. Trong nhóm này, UC06 có sự tham gia của các tác nhân ngoài hệ thống vì việc thanh toán và gửi thư xác nhận cần trao đổi dữ liệu với dịch vụ PayOS và Mail Server.

UC07, UC08 và UC09 thể hiện định hướng nâng cao trải nghiệm người đọc và phát triển hệ sinh thái sách tuần hoàn. UC07 hỗ trợ người dùng tìm sách theo nhu cầu thay vì chỉ theo danh mục cố định; UC08 tạo quy trình đưa sách đã qua sử dụng trở lại lưu thông; UC09 duy trì sự gắn kết của khách hàng với hoạt động đọc sách. Các use case này sẽ được triển khai, hoàn thiện theo kế hoạch phát triển của đề tài và được phân tích cùng các yêu cầu dữ liệu liên quan.

UC10, UC11 và UC12 tập trung vào hoạt động vận hành và quản trị. Nhóm use case này cho phép người bán/quản trị viên duy trì dữ liệu sách, xử lý các sự kiện phát sinh, theo dõi đơn hàng và khai thác thông tin tổng hợp. Sự liên kết giữa các nhóm use case bảo đảm hệ thống đáp ứng cả nhu cầu mua sách của khách hàng lẫn nhu cầu quản lý của cửa hàng.
